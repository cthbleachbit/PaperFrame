package me.cth451.paperframe.task;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

/**
 * For all active frame highlight users, collect the set of nearby frames, draw particles on them.
 */
public class ActivePlayerUpdate implements Runnable {
	private final PaperFramePlugin plugin;
	public static final Particle.DustOptions option = new Particle.DustOptions(Color.WHITE, 1.0f);
	public static final int HIGHLIGHT_RANGE = 10;

	public ActivePlayerUpdate(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		HashSet<UUID> framesInRange = new HashSet<UUID>();
		synchronized (PaperFramePlugin.activeHighlightUsers) {
			for (UUID playerID : PaperFramePlugin.activeHighlightUsers) {
				Player player = Bukkit.getPlayer(playerID);
				if (player == null) {
					PaperFramePlugin.activeHighlightUsers.remove(playerID);
					continue;
				}

				for (Entity entity : player.getNearbyEntities(HIGHLIGHT_RANGE, HIGHLIGHT_RANGE, HIGHLIGHT_RANGE)) {
					if (entity instanceof ItemFrame) {
						framesInRange.add(entity.getUniqueId());
					}
				}
			}
		}

		for (UUID frameID : framesInRange) {
			ItemFrame frame = (ItemFrame) Bukkit.getEntity(frameID);
			if (frame == null) {
				continue;
			}
			Drawing.drawBoundingBox(frame.getBoundingBox(), frame.getWorld(), option);
		}
	}
}
