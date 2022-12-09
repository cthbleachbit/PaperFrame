package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.FrameProperties;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

import static me.cth451.paperframe.util.FrameProperties.getHidden;
import static me.cth451.paperframe.util.Targeting.findFrameByTargetedEntity;

/**
 * Reveal / Hide an item frame. This command make use of internal
 */
public class FrameShowHide implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	public FrameShowHide(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	public static void setShowHideByPlayer(@NotNull ItemFrame frame, boolean visible, @NotNull Player player) {
		FrameProperties.setHidden(frame, !visible);
		FrameProperties.setHiddenBy(frame, !visible ? player.getName() : null);
		FrameProperties.setHiddenAt(frame, !visible ? new Date() : null);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		// Check whether the player is looking at an item frame
		ItemFrame frame = findFrameByTargetedEntity(player);

		if (frame == null) {
			// No item frames in range
			player.sendMessage("Can't find an item frame where you are looking at");
		} else {
			boolean isHidden = getHidden(frame);
			setShowHideByPlayer(frame, isHidden, player);
			final Particle.DustOptions options = new Particle.DustOptions(isHidden ? Color.GREEN : Color.RED, 1.0f);
			player.sendMessage(isHidden ? "Frame revealed" : "Frame hidden");
			Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 3, 10);
		}
		return true;
	}
}
