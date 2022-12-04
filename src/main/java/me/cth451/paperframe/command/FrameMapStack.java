package me.cth451.paperframe.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.FrameProperties;
import me.cth451.paperframe.util.Targeting;
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
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * fmaps|gfmaps [-g] id1 id2 id3....
 * <p>
 * Spawn multiple item frames to contain maps of specified ids. Note that this will delete any item frames that already
 * occupy that block surface. The newly generated frames will be hidden and protected by default.
 * <p>
 * Use gfmaps to spawn glowing item frames instead of normal ones.
 */
public class FrameMapStack implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	public FrameMapStack(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	private void nukeAllFramesLookedAt(@NotNull BoundingBox boundingBox, @NotNull Player player) {
		List<Entity> nearby = player.getNearbyEntities(Targeting.SELECTION_RANGE, Targeting.SELECTION_RANGE,
		                                               Targeting.SELECTION_RANGE);
		final BoundingBox fBox = boundingBox;
		List<ItemFrame> filtered =
				nearby.stream()
				      .filter(ItemFrame.class::isInstance)
				      .filter(e -> fBox.contains(e.getLocation().toVector()))
				      .map(ItemFrame.class::cast)
				      .toList();

		plugin.getLogger().info(filtered.toString());
		filtered.forEach(Entity::remove);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1p) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		if (argv1p.length == 0) {
			return false;
		}

		final boolean useGlow = argv0.equals("gfmaps");
		List<Short> ids;
		try {
			ids = Arrays.stream(argv1p).map(Short::parseShort).toList();
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
		final BoundingBox containingBox =
				new BoundingBox(containingBlock.getX(), containingBlock.getY(), containingBlock.getZ(),
				                containingBlock.getX() + 1, containingBlock.getY() + 1, containingBlock.getZ() + 1);

		nukeAllFramesLookedAt(containingBox, player);

		final Player finalPlayer = player;
		final BlockFace extruding = targetInfo.getBlockFace();
		final Block affixedBlock = targetInfo.getBlock();
		final EntityType frameType = useGlow ? EntityType.GLOW_ITEM_FRAME : EntityType.ITEM_FRAME;

		/* Create new item frames */
		long created =
				ids.stream()
				   .distinct()
				   .filter(id -> Bukkit.getMap(id) != null)
				   .filter(id -> {
					   Entity e = finalPlayer.getWorld().spawnEntity(containingBlock, frameType);
					   ItemFrame f = (ItemFrame) e;
					   ItemStack content = new ItemStack(Material.FILLED_MAP, 1);
					   MapMeta meta = (MapMeta) content.getItemMeta();
					   meta.setMapId(id);
					   content.setItemMeta(meta);
					   f.setItem(content);
					   f.setFacingDirection(extruding, true);
					   FrameProtect.setProtected(f, true, player);
					   FrameShowHide.setShowHide(f, false, player);
					   return true;
				   })
				   .count();

		player.sendMessage(String.format("Created %d item frames", created));
		return true;
	}
}
