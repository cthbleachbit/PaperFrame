package me.cth451.paperframe.task;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.HighlightOptions;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * For all active frame highlight users, collect the set of nearby frames, draw particles on them.
 */
public class FrameHighlightTask implements Runnable {
	private final PaperFramePlugin plugin;
	public static final Particle.DustOptions option = new Particle.DustOptions(Color.WHITE, 1.0f);

	public FrameHighlightTask(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		/* Need to use hash set to ensure duplicate frames are not added */
		HashSet<ItemFrame> frames = new HashSet<>();

		synchronized (PaperFramePlugin.activeHighlightUsers) {
			for (Map.Entry<UUID, HighlightOptions> entry : PaperFramePlugin.activeHighlightUsers.entrySet()) {
				UUID playerID = entry.getKey();
				HighlightOptions options = entry.getValue();
				double range = options.range;

				Player player = Bukkit.getPlayer(playerID);
				// Just in case that a player has left
				if (player == null) {
					PaperFramePlugin.activeHighlightUsers.remove(playerID);
					continue;
				}

				List<ItemFrame> nearby = options.source(player);
				frames.addAll(nearby.stream()
				                    .filter(options)
				                    .toList());
			}

			if (PaperFramePlugin.activeHighlightUsers.isEmpty()) {
				// Stop background update when no players are requesting service
				this.plugin.stopPlayerUpdate();
			}
		}

		frames.forEach((frame) -> Drawing.drawBoundingBox(frame, option));
	}
}
