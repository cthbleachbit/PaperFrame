package me.cth451.paperframe.eventlistener;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.FrameProperties;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Perform checks pertaining frame protection. A protected frame:
 * <ul>
 *     <li>
 *         Cannot have its contents replaced or rotated (i.e. on player right click).
 *     </li>
 *     <li>
 *         Cannot be destroyed by taking damage from entity (player damage, explosions, etc).
 *     </li>
 *     <li>
 *         Cannot be destroyed by removing the supporting block or placing a block in the occupying space.
 *     </li>
 * </ul>
 */
public class FrameProtectListener implements Listener {
	private final PaperFramePlugin plugin;

	private final Particle.DustOptions options = new Particle.DustOptions(Color.BLACK, 1.0f);

	public FrameProtectListener(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Catch player right click, prevent changes if the frame is protected
	 *
	 * @param event right click event from player.
	 */
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof ItemFrame frame)) {
			return;
		}

		if (!FrameProperties.getProtected(frame)) {
			return;
		}

		event.setCancelled(true);
		event.getPlayer().sendMessage("This frame is protected!");
		Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 1, 10);
	}

	/**
	 * Catch occasions where the frame would be removed
	 *
	 * @param event the event leading to removal of this item frame
	 */
	@EventHandler
	public void onHangingBreakEvent(HangingBreakEvent event) {
		if (!(event.getEntity() instanceof ItemFrame frame)) {
			return;
		}

		if (!FrameProperties.getProtected(frame)) {
			return;
		}

		event.setCancelled(true);
		if (event instanceof HangingBreakByEntityEvent breakByEntityEvent) {
			if (breakByEntityEvent.getRemover() instanceof Player player) {
				player.sendMessage("This frame is protected!");
				Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 1, 10);
			}
		}
	}

	/**
	 * Catch occasions where the frame gets a hit
	 *
	 * @param event damage event (might not come from a player)
	 */
	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof ItemFrame frame)) {
			return;
		}

		if (!FrameProperties.getProtected(frame)) {
			return;
		}

		event.setCancelled(true);
		if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
			if (damageByEntityEvent.getDamager() instanceof Player player) {
				player.sendMessage("This frame is protected!");
			}
		}

		Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 1, 10);
	}
}
