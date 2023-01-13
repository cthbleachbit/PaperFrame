package me.cth451.paperframe.dependency;

import me.cth451.paperframe.PaperFramePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tracks API calls into other soft dependencies.
 */
public class DependencyManager implements IWorldEdit {

	private final PaperFramePlugin plugin;
	private IWorldEdit worldEditAPI = null;

	/**
	 * Initialize and probe existence of any optional dependencies
	 *
	 * @param plugin base plugin
	 */
	public DependencyManager(PaperFramePlugin plugin) {
		this.plugin = plugin;
		/* Check WorldEdit availability */
		if (this.plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
			worldEditAPI = new WorldEditLink(plugin);
		}
	}

	/**
	 * Check whether WorldEdit has been found here. Other classes should call this function before calling
	 * {@link DependencyManager#getCuboidSelection(Player)} and
	 * {@link DependencyManager#getCuboidSelection(Player, boolean)}. If a player is specified, they will receive an
	 * error message if WorldEdit is not present.
	 *
	 * @param player requesting player
	 * @return whether WorldEdit is functional and callable
	 */
	public boolean isWorldEditAvailable(@Nullable Player player) {
		if (worldEditAPI == null) {
			if (player != null) {
				player.sendMessage(ChatColor.RED + "WorldEdit is not found on this server. Cannot use `-w`.");
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Wrapper for {@link WorldEditLink#getCuboidSelection(Player)}
	 *
	 * @param player requesting player
	 * @return null if API is not present or list of item frames
	 */
	@Override
	public List<ItemFrame> getCuboidSelection(@NotNull Player player) {
		if (worldEditAPI == null) return null;
		return worldEditAPI.getCuboidSelection(player);
	}

	/**
	 * Wrapper for {@link WorldEditLink#getCuboidSelection(Player, boolean)}
	 *
	 * @param player requesting player
	 * @return null if API is not present or list of item frames
	 */
	@Override
	public List<ItemFrame> getCuboidSelection(@NotNull Player player, boolean interactive) {
		if (worldEditAPI == null) return null;
		return worldEditAPI.getCuboidSelection(player, interactive);
	}
}
