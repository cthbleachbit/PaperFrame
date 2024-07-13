package me.cth451.paperframe.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import it.unimi.dsi.fastutil.Hash;
import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.IdRange;
import me.cth451.paperframe.util.Targeting;
import me.cth451.paperframe.util.exceptions.InvalidFrameCountException;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.PrintHelpException;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
 */
public class Frame2d implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	private final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("height", 'h', UnixFlagSpec.FlagType.PARAMETRIZE, "height", Integer::parseUnsignedInt),
			new UnixFlagSpec("width", 'w', UnixFlagSpec.FlagType.PARAMETRIZE, "width", Integer::parseUnsignedInt),
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
		try {
			parsed = argvParser.parse(List.of(argv1), id_specs);
		} catch (IllegalArgumentException | PrintHelpException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			player.sendMessage(ChatColor.YELLOW + command.getDescription());
			player.sendMessage(command.getUsage());
			return true;
		}
		int height = (int) parsed.getOrDefault("height", 1);
		int width = (int) parsed.getOrDefault("width", 1);

		List<Integer> ids;
		try {
			ids = IdRange.parseIdRanges(id_specs);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			return true;
		}

		/* Sanity check: id max */
		Optional<Integer> bad_id = ids.stream().filter(id -> Bukkit.getMap(id) == null).findAny();
		if (bad_id.isPresent()) {
			player.sendMessage(ChatColor.YELLOW + String.format("Map %d not found", bad_id.get()));
			return true;
		}

		/* Get targeting info */
		final TargetBlockInfo targetInfo = player.getTargetBlockInfo(Targeting.SELECTION_RANGE,
		                                                             TargetBlockInfo.FluidMode.NEVER);
		if (targetInfo == null) {
			player.sendMessage(ChatColor.YELLOW + "Cannot find a block under cursor");
			return true;
		}

		List<ItemFrame> targets;
		try {
			targets = Targeting.byRectangleTopLeftCorner(targetInfo.getBlock(),
			                                             targetInfo.getBlockFace(),
			                                             width, height);
		} catch (InvalidFrameCountException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			e.scheduleStickyDraw(plugin);
			return true;
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			return true;
		}

		/* Sanity check: fill area size */
		if (targets.size() < ids.size()) {
			player.sendMessage(ChatColor.YELLOW + String.format(
					"Not enough item frames found on the wall. Need %d frames, found %d frames.",
					ids.size(),
					targets.size()));
			return true;
		} else if (targets.size() > ids.size()) {
			player.sendMessage(ChatColor.YELLOW + String.format(
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
}
