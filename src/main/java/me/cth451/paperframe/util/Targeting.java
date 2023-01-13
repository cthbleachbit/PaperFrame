package me.cth451.paperframe.util;

import com.destroystokyo.paper.block.TargetBlockInfo;
import me.cth451.paperframe.PaperFramePlugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Targeting {
	/**
	 * Range to find any frames to show/hide
	 */
	public static final int SELECTION_RANGE = 6;

	/**
	 * Find an item frame that a player is looking at.
	 * <p>
	 * Note that this function can only return one targeted entity. To find multiple item frames stacked in the same
	 * space created from /framemaps, use {@link Targeting#byTargetedStackedEntity} instead.
	 *
	 * @param player the player to check
	 * @return ONE OF the item frames player is looking at.
	 */
	public static ItemFrame byTargetedEntity(@NotNull Player player) {
		Entity entity = player.getTargetEntity(SELECTION_RANGE);
		if (entity == null) {
			return null;
		}

		if (!(entity instanceof ItemFrame frame)) {
			return null;
		}

		return frame;
	}

	/**
	 * Find ALL item frames that a player is looking at.
	 * <p>
	 * To find item frames based on targeted block, use {@link Targeting#byAttachedBlockFace} instead.
	 *
	 * @param player the player to check
	 * @return list of item frames player is looking at. The returned list is unmodifiable.
	 */
	public static List<ItemFrame> byTargetedStackedEntity(@NotNull Player player) {
		/* Find one first */
		Entity entity = player.getTargetEntity(SELECTION_RANGE);

		if (entity == null) {
			return Collections.emptyList();
		}

		if (!(entity instanceof ItemFrame frame)) {
			return Collections.emptyList();
		}

		/* Find all in the same region */
		return frame.getWorld()
		            .getNearbyEntitiesByType(ItemFrame.class, frame.getLocation(), 0.015625d)
		            .stream()
		            .toList();
	}

	/**
	 * Find all item frame that is attached to a block on a specific face that the player is staring
	 *
	 * @param player the player to check
	 * @return a list of item frames that exist on that block face. The returned list is unmodifiable.
	 */
	public static List<ItemFrame> byAttachedBlockFace(@NotNull Player player) {
		TargetBlockInfo targetInfo = player.getTargetBlockInfo(Targeting.SELECTION_RANGE,
		                                                       TargetBlockInfo.FluidMode.NEVER);
		if (targetInfo == null) {
			return Collections.emptyList();
		}

		if (targetInfo.getBlock().getType().isAir()) {
			return Collections.emptyList();
		}

		final Block targetBlock = targetInfo.getBlock();
		final BlockFace face = targetInfo.getBlockFace();
		return byAttachedBlockFace(targetBlock, face);
	}

	/**
	 * Find all item frame that is contained in a bounding box
	 *
	 * @param boundingBox box
	 * @param world       world to search in
	 * @return a list of item frames that exist on that block face. The returned list is unmodifiable.
	 */
	public static List<ItemFrame> byBoundingBox(@NotNull BoundingBox boundingBox, @NotNull World world) {
		return world.getEntitiesByClass(ItemFrame.class)
		            .stream()
		            .filter(e -> boundingBox.contains(e.getLocation().toVector()))
		            .toList();
	}

	/**
	 * Find all item frame that is attached to a block on a specific face.
	 *
	 * @param block the block to check
	 * @param face  the face to check
	 * @return a list of item frames attached to the specific block on the specific face. The returned list is
	 * 		unmodifiable.
	 */
	public static List<ItemFrame> byAttachedBlockFace(@NotNull Block block, @NotNull BlockFace face) {
		final Block containingBlock = block.getRelative(face);
		final BoundingBox containingBox =
				new BoundingBox(containingBlock.getX(), containingBlock.getY(), containingBlock.getZ(),
				                containingBlock.getX() + 1, containingBlock.getY() + 1, containingBlock.getZ() + 1);
		final BlockFace fFace = face;
		return byBoundingBox(containingBox, block.getWorld())
				.stream()
				.filter(e -> e.getFacing() == fFace)
				.toList();
	}

	/**
	 * Find all item frames within range of player WorldEdit selection. This method will return null if WorldEdit is not
	 * active on this server.
	 *
	 * @param player requesting player
	 * @return the list of item frames within WorldEdit selection range (maybe empty), or null if and only id not active.
	 */
	public static List<ItemFrame> byWorldEditCuboid(@NotNull Player player) {
		if (PaperFramePlugin.WorldEdit == null) {
			player.sendMessage(ChatColor.RED + "WorldEdit is not found on this server. Cannot use `-w`.");
			return Collections.emptyList();
		} else {
			return PaperFramePlugin.WorldEdit.getCuboidSelection(player);
		}
	}
}
