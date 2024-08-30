package me.cth451.paperframe.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.collect.Iterables;
import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.command.base.IAsyncTabCompleteExecutor;
import me.cth451.paperframe.util.IdRange;
import me.cth451.paperframe.util.Targeting;
import me.cth451.paperframe.util.exceptions.InvalidFrameCountException;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.ParameterRequiredException;
import me.cth451.paperframe.util.getopt.PrintHelpException;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import me.cth451.paperframe.util.tileviewer.DirectoryListing;
import me.cth451.paperframe.util.tileviewer.GroupMetadata;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.max;
import static me.cth451.paperframe.util.getopt.Unescape.escape;

/**
 * Place tiles one by one onto a flat rectangle of frames with top-left corner under cursor. All frames must already exist.
 * <br>
 * Existing items in the frames will be deleted and replaced. All map ids will be validated prior to execution.
 * <p>
 * Map IDs can be specified in the following ways:
 * <ul>
 *     <li>"X" This exact map with ID exactly equal to X</li>
 *     <li>"X:Y" Maps between X and Y inclusive in that order</li>
 *     <li>"X+N"  X and upwards to X+N (inclusive)</li>
 *     <li>"X+-N" X+N and downwards to X (inclusive)</li>
 * </ul>
 * Alternatively pass "-n tile_set_name" to fetch tile definitions from tileset viewer REST API.
 */
public class Frame2d implements CommandExecutor, IAsyncTabCompleteExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	private final static UnixFlagSpec nameFlagSpec = new UnixFlagSpec("name",
	                                                                  'n',
	                                                                  UnixFlagSpec.FlagType.PARAMETRIZE,
	                                                                  "name");

	private final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("height", 'h', UnixFlagSpec.FlagType.PARAMETRIZE, "height", Integer::parseUnsignedInt),
			new UnixFlagSpec("width", 'w', UnixFlagSpec.FlagType.PARAMETRIZE, "width", Integer::parseUnsignedInt),
			/* Future - Get map information by name */
			nameFlagSpec,
	};

	protected final static ArgvParser argvParser = new ArgvParser(List.of(arguments));

	protected final static Particle.DustOptions options = new Particle.DustOptions(Color.RED, 1.0f);

	public Frame2d(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender,
	                         @NotNull Command command,
	                         @NotNull String argv0,
	                         @NotNull String[] argv1) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player");
			return false;
		}

		List<String> id_specs = new LinkedList<>();
		HashMap<String, Object> parsed;
		List<Integer> ids;
		try {
			parsed = argvParser.parse(List.of(argv1), id_specs);
		} catch (IllegalArgumentException | PrintHelpException | ParameterRequiredException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			player.sendMessage(ChatColor.YELLOW + command.getDescription());
			player.sendMessage(command.getUsage());
			return true;
		}
		int height = (int) parsed.getOrDefault("height", 1);
		int width = (int) parsed.getOrDefault("width", 1);

		final String tilesetName = (String) parsed.getOrDefault("name", null);
		if (tilesetName != null) {
			GroupMetadata metadata;
			try {
				metadata = this.plugin.getTileSetViewerClient().checkMetadataCache(tilesetName);
			} catch (IllegalStateException e) {
				player.sendMessage(ChatColor.RED + e.getMessage());
				return true;
			}
			if (metadata == null) {
				/* FIXME: Schedule background fetch and notify when ready */
				String playerName = player.getName();
				player.sendMessage(ChatColor.YELLOW + "Retrieving tileset metadata for " + tilesetName);
				this.plugin.getTileSetViewerClient().scheduleMetadataFetch(
						this.plugin,
						tilesetName,
						(success, message) -> {
							Player notifyPlayer = Bukkit.getPlayer(playerName);
							if (notifyPlayer == null) {
								this.plugin.getComponentLogger().error("Unable to send message to player {}",
								                                       playerName);
								return;
							}
							notifyPlayer.sendMessage((success ? ChatColor.GREEN : ChatColor.RED) + message);
						});
				return true;
			}
			/* Override ids, width and height */
			height = metadata.geometry.size();
			width = metadata.geometry.get(0).size();
			ids = new LinkedList<>();
			metadata.geometry.forEach(ids::addAll);
			player.sendMessage(ChatColor.GREEN + String.format("Using tileset %s with %d rows %d columns",
			                                                   tilesetName,
			                                                   height,
			                                                   width));
		} else {
			try {
				ids = IdRange.parseIdRanges(id_specs);
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + e.getMessage());
				return true;
			}
		}

		/* Sanity check: id max */
		Optional<Integer> bad_id = ids.stream().filter(id -> Bukkit.getMap(id) == null).findAny();
		if (bad_id.isPresent()) {
			player.sendMessage(ChatColor.RED + String.format("Map %d not found", bad_id.get()));
			return true;
		}

		/* Get targeting info */
		final TargetBlockInfo targetInfo = player.getTargetBlockInfo(Targeting.SELECTION_RANGE,
		                                                             TargetBlockInfo.FluidMode.NEVER);
		if (targetInfo == null) {
			player.sendMessage(ChatColor.RED + "Cannot find a block under cursor");
			return true;
		}

		List<ItemFrame> targets;
		try {
			targets = Targeting.byRectangleTopLeftCorner(targetInfo.getBlock(),
			                                             targetInfo.getBlockFace(),
			                                             width, height);
		} catch (InvalidFrameCountException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			e.scheduleStickyDraw(plugin);
			return true;
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return true;
		}

		/* Sanity check: fill area size */
		if (targets.size() < ids.size()) {
			player.sendMessage(ChatColor.RED + String.format(
					"Not enough item frames found on the wall. Need %d frames, found %d frames.",
					ids.size(),
					targets.size()));
			return true;
		} else if (targets.size() > ids.size()) {
			player.sendMessage(ChatColor.RED + String.format(
					"Not enough map IDs given on command line. Need %d IDs, found %d IDs.",
					targets.size(),
					ids.size()));
			return true;
		}

		/* Execute fill */
		for (int i = 0; i < targets.size(); i++) {
			ItemFrame frame = targets.get(i);
			int id = ids.get(i);
			ItemStack content = new ItemStack(Material.FILLED_MAP, 1);
			MapMeta meta = (MapMeta) content.getItemMeta();
			meta.setMapId(id);
			content.setItemMeta(meta);
			frame.setItem(content);
		}

		return true;
	}

	@Override
	public List<AsyncTabCompleteEvent.Completion> onTabComplete(CommandSender sender, List<String> argv1p,
	                                                            boolean lastArgComplete) {
		List<AsyncTabCompleteEvent.Completion> completions;
		try {
			completions = new LinkedList<>(argvParser.tabComplete(argv1p, lastArgComplete));
		} catch (ParameterRequiredException e) {
			completions = new LinkedList<>();
			if (e.getSpec().equals(nameFlagSpec)) {
				/* Request completion via api client*/
				DirectoryListing listing;
				String query = lastArgComplete ? "" : Iterables.getLast(argv1p);
				List<String> pathComponents = List.of(query.split("/", -1));
				String partialPrefix = String.join("/", pathComponents.subList(0, max(0, pathComponents.size() - 1)));
				String trailingComponent = pathComponents.get(max(0, pathComponents.size() - 1));

				try {
					listing = this.plugin.getTileSetViewerClient().listPrefix("%s/".formatted(partialPrefix));
				} catch (IOException ex) {
					this.plugin.getComponentLogger().error("Failed enumerating tile sets", ex);
					return List.of();
				}

				String currentPath = partialPrefix.isEmpty() ? "" : partialPrefix + "/";
				listing.directories.stream()
				                   .filter((s) -> s.startsWith(trailingComponent))
				                   .map((s) -> new CompletionResult(
						                   IAsyncTabCompleteExecutor.escapeForCompletion(currentPath) + escape(s) + "/",
						                   Component.text("subdirectory")))
				                   .forEachOrdered(completions::add);
				listing.tilesets.stream()
				                .filter((s) -> s.startsWith(trailingComponent))
				                .map((s) -> new CompletionResult(
						                IAsyncTabCompleteExecutor.escapeForCompletion(currentPath) + escape(s),
						                Component.text("tile set")))
				                .forEachOrdered(completions::add);
			}

		}
		return completions;
	}
}
