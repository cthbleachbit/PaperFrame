package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FrameShowHide implements CommandExecutor {
	private PaperFramePlugin plugin;
	public static final int SELECTION_RANGE = 5;

	public FrameShowHide(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if (!(commandSender instanceof Player player)) {
			return false;
		}

		// Check whether the player is looking at an item frame
		ItemFrame frame = null;
		float bestAngle = 0.2f;

		Vector playerLookDir = player.getEyeLocation().getDirection();
		Vector playerEyeLoc = player.getEyeLocation().toVector();

		for (Entity entity : player.getNearbyEntities(SELECTION_RANGE,SELECTION_RANGE,SELECTION_RANGE)) {
			if (!(entity instanceof ItemFrame)) {
				continue;
			}
			Vector frameLoc = entity.getLocation().toVector();
			Vector playerEntityVec = frameLoc.subtract(playerEyeLoc);
			float angle = playerLookDir.angle(playerEntityVec);

			if (bestAngle > angle) {
				frame = (ItemFrame) entity;
				bestAngle = angle;
			}
		}

		if (frame == null) {
			// No item frames in range
			player.sendMessage("You need to look at an item frame to hide/unhide it");
		} else {
			boolean isVisible = frame.isVisible();
			Particle.DustOptions options = null;
			frame.setVisible(!isVisible);
			if (isVisible) {
				player.sendMessage("Frame hidden");
				options = new Particle.DustOptions(Color.RED, 1.0f);
			} else {
				player.sendMessage("Frame unhidden");
				options = new Particle.DustOptions(Color.GREEN, 1.0f);
			}
			ItemFrame finalFrame = frame;
			Drawing.scheduleStickyDraw(this.plugin, frame, options, 3, 10);
		}
		return true;
	}
}
