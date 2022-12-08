package me.cth451.paperframe.util;

import com.destroystokyo.paper.block.TargetBlockInfo;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
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
	 * space created from /framemaps, use {@link Targeting#findFrameByAttachedBlockFace} instead.
	 *
	 * @param player the player to check
	 * @return ONE OF the item frames player is looking at.
	 */
	public static ItemFrame findFrameByTargetedEntity(@NotNull Player player) {
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
	 * Find all item frame that is attached to a block on a specific face that the player is staring
	 *
	 * @param player the player to check
	 * @return a list of item frames that exist on that block face
	 */
	public static List<ItemFrame> findFrameByAttachedBlockFace(@NotNull Player player) {
		TargetBlockInfo targetInfo = player.getTargetBlockInfo(Targeting.SELECTION_RANGE,
		                                                       TargetBlockInfo.FluidMode.NEVER);
		if (targetInfo == null) {
			return new LinkedList<>();
		}

		if (targetInfo.getBlock().getType().isAir()) {
			return new LinkedList<>();
		}

		final Block targetBlock = targetInfo.getBlock();
		final BlockFace face = targetInfo.getBlockFace();
		return findFrameByAttachedBlockFace(targetBlock, face);
	}

	/**
	 * Find all item frame that is attached to a block on a specific face.
	 *
	 * @param block the block to check
	 * @param face  the face to check
	 * @return the list of item frames attached to the specific block on the specific face
	 */
	public static List<ItemFrame> findFrameByAttachedBlockFace(@NotNull Block block, @NotNull BlockFace face) {
		final Block containingBlock = block.getRelative(face);
		final BoundingBox containingBox =
				new BoundingBox(containingBlock.getX(), containingBlock.getY(), containingBlock.getZ(),
				                containingBlock.getX() + 1, containingBlock.getY() + 1, containingBlock.getZ() + 1);
		final BlockFace fFace = face;
		return block.getWorld().getEntitiesByClass(ItemFrame.class)
		            .stream()
		            .filter(e -> containingBox.contains(e.getLocation().toVector()))
		            .filter(e -> e.getFacing() == fFace)
		            .toList();
	}
}
