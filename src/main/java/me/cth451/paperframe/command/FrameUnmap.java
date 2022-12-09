package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Targeting;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.cth451.paperframe.util.Targeting.findStackedFrameByTargetedEntity;

public class FrameUnmap implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	public FrameUnmap(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Remove all item frames hanging on the specific block and on the specific face.
	 *
	 * @param player the player to check
	 * @return number of item frames removed
	 */
	public static long removeFramesTargetedBlockFace(@NotNull Player player) {
		List<ItemFrame> filtered = Targeting.findFrameByAttachedBlockFace(player);
		filtered.forEach(Entity::remove);
		return filtered.size();
	}

	/**
	 * Remove all item frames by targeted entity - the frames might be in the air and not attached to a block.
	 *
	 * @param player the player to check
	 * @return number of item frames removed
	 */
	public static long removeFramesTargetedEntity(@NotNull Player player) {
		List<ItemFrame> filtered = findStackedFrameByTargetedEntity(player);
		filtered.forEach(Entity::remove);
		return filtered.size();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player");
			return false;
		}

		long removed = removeFramesTargetedEntity(player);
		player.sendMessage(String.format("Removed %d item frames", removed));
		return true;
	}
}
