package me.cth451.paperframe.listener;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class FrameDestroyListener implements Listener {
	@EventHandler
	public void onEntityDeath(HangingBreakEvent deathEvent) {
		// Only handle item frame death
		if (!(deathEvent.getEntity() instanceof ItemFrame frame)) {
			return;
		}
	}
}
