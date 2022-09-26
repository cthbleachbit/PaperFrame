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
public class ActivePlayerUpdate implements Runnable {
	private final PaperFramePlugin plugin;
	public static final Particle.DustOptions option = new Particle.DustOptions(Color.WHITE, 1.0f);


	public ActivePlayerUpdate(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		HashSet<ItemFrame> frames = new HashSet<>();
		synchronized (PaperFramePlugin.activeHighlightUsers) {
			for (Map.Entry<UUID, HighlightOptions> entry : PaperFramePlugin.activeHighlightUsers.entrySet()) {
				UUID playerID = entry.getKey();
				boolean hiddenOnly = entry.getValue().hiddenOnly;
				double range = entry.getValue().range;

				Player player = Bukkit.getPlayer(playerID);
				if (player == null) {
					PaperFramePlugin.activeHighlightUsers.remove(playerID);
					continue;
				}

				List<Entity> nearby = player.getNearbyEntities(range, range, range);
				frames.addAll(nearby.stream()
				                    .filter((e) -> e instanceof ItemFrame frame && (!hiddenOnly || !frame.isVisible()))
				                    .map((e) -> (ItemFrame) e).toList());
			}
		}


		for (ItemFrame frame : frames) {
			Drawing.drawBoundingBox(frame, option);
		}
	}
}
