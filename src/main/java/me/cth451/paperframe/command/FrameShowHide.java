package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.cth451.paperframe.util.Targeting.findFrameByTargetedEntity;

public class FrameShowHide implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	public FrameShowHide(PaperFramePlugin plugin) {
		this.plugin = plugin;
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
			boolean isVisible = frame.isVisible();
			frame.setVisible(!isVisible);
			final Particle.DustOptions options = new Particle.DustOptions(isVisible ? Color.RED : Color.GREEN, 1.0f);
			player.sendMessage(isVisible ? "Frame hidden" : "Frame revealed");
			Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 3, 10);
		}
		return true;
	}
}
