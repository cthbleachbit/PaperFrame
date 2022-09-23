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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

import static me.cth451.paperframe.PaperFramePlugin.SELECTION_RANGE;

/**
 * highlight the frames in range even when they are hidden, and turn off in 5 seconds
 */
public class FrameHighlight implements CommandExecutor {
	private PaperFramePlugin plugin;

	public FrameHighlight(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			return false;
		}

		LinkedList<ItemFrame> framesHighlighted = new LinkedList<>();
		for (Entity entity : player.getNearbyEntities(SELECTION_RANGE, SELECTION_RANGE, SELECTION_RANGE)) {
			if (!(entity instanceof ItemFrame)) {
				continue;
			}
			framesHighlighted.add((ItemFrame) entity);
		}

		if (framesHighlighted.isEmpty()) {
			return true;
		}

		Particle.DustOptions options = new Particle.DustOptions(Color.WHITE, 1.0F);
		for (ItemFrame frame : framesHighlighted) {
			Drawing.scheduleStickyDraw(this.plugin, frame, options, 20, 10);
		}

		player.sendMessage(String.format("Highlighting %s item frames", framesHighlighted.size()));
		return true;
	}
}
