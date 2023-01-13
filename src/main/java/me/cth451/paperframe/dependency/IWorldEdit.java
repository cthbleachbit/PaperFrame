package me.cth451.paperframe.dependency;

import org.bukkit.entity.ItemFrame;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Required APIs that we need to implement from WorldEdit primitives.
 */
public interface IWorldEdit {
	/**
	 * Get frames within a cuboid
	 *
	 * @param player requesting player
	 * @return list of item frames contained in the selection, or empty list if the selection is not a cube
	 */
	List<ItemFrame> getCuboidSelection(@NotNull org.bukkit.entity.Player player);

	/**
	 * Get frames within a cuboid
	 *
	 * @param player      requesting player
	 * @param interactive whether the player should be prompted to make a selection
	 * @return list of item frames contained in the selection, or empty list if the selection is not a cube
	 */
	List<ItemFrame> getCuboidSelection(@NotNull org.bukkit.entity.Player player, boolean interactive);
}
