package me.cth451.paperframe.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Targeting;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static me.cth451.paperframe.command.FrameUnmap.removeFramesTargeted;

/**
 * framemaps [-g] [-a] id1 id2 id3....
 * <p>
 * Spawn multiple item frames to contain maps of specified ids. Note that this will delete any item frames that already
 * occupy that block surface if [-a] is not specified. Generated frames will be hidden and protected by default.
 * <ul>
 *     <li>-g = glow item frame</li>
 *     <li>-a = append</li>
 * </ul>
 */
public class FrameMapStack implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	/**
	 * Constructor
	 *
	 * @param plugin ptr to plugin instance, mainly used for logger
	 */
	public FrameMapStack(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	private final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("glow", 'g', UnixFlagSpec.FlagType.EXIST, "glow"),
			new UnixFlagSpec("append", 'a', UnixFlagSpec.FlagType.EXIST, "append"),
	};

	private final static ArgvParser argvParser = new ArgvParser(List.of(arguments));


	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1p) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		if (argv1p.length == 0) {
			return false;
		}

		HashMap<String, Object> parsed;
		List<String> extraArgs = new LinkedList<>();
		parsed = argvParser.parse(List.of(argv1p), extraArgs);

		final boolean useGlow = (Boolean) parsed.get("glow");
		final boolean append = (Boolean) parsed.get("append");

		List<Short> ids;
		try {
			ids = extraArgs.stream().map(Short::parseShort).toList();
		} catch (NumberFormatException e) {
			return false;
		}

		TargetBlockInfo targetInfo = player.getTargetBlockInfo(Targeting.SELECTION_RANGE,
		                                                       TargetBlockInfo.FluidMode.NEVER);
		if (targetInfo == null) {
			return false;
		}

		if (targetInfo.getBlock().getType().isAir()) {
			return false;
		}

		final Block targetBlock = targetInfo.getBlock();
		final BlockFace face = targetInfo.getBlockFace();
		final Location containingBlock = targetBlock.getRelative(face, 1).getLocation();

		if (!append) {
			long removed = removeFramesTargeted(player);
			if (removed > 0)
				player.sendMessage(String.format("Removed %d item frames", removed));
		}

		final Player finalPlayer = player;
		final BlockFace extruding = targetInfo.getBlockFace();
		final EntityType frameType = useGlow ? EntityType.GLOW_ITEM_FRAME : EntityType.ITEM_FRAME;

		/* Create new item frames */
		long created =
				ids.stream()
				   .distinct()
				   /* Make sure the map id exist on the server */
				   .filter(id -> Bukkit.getMap(id) != null)
				   /* Spawn the item frame and place map with specific id in the frame */
				   .filter(id -> {
					   Entity e = finalPlayer.getWorld().spawnEntity(containingBlock, frameType);
					   ItemFrame f = (ItemFrame) e;
					   ItemStack content = new ItemStack(Material.FILLED_MAP, 1);
					   MapMeta meta = (MapMeta) content.getItemMeta();
					   meta.setMapId(id);
					   content.setItemMeta(meta);
					   f.setItem(content);
					   f.setFacingDirection(extruding, true);
					   FrameProtect.setProtectedByPlayer(f, true, player);
					   FrameShowHide.setShowHideByPlayer(f, false, player);
					   return true;
				   })
				   .count();

		player.sendMessage(String.format("Created %d item frames", created));
		return true;
	}
}
