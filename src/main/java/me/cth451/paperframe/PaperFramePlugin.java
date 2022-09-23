package me.cth451.paperframe;

import me.cth451.paperframe.command.*;
import me.cth451.paperframe.listener.*;
import me.cth451.paperframe.task.*;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PaperFramePlugin extends JavaPlugin {
	private FrameDestroyListener frameDestroyListener = null;
	private int activePlayerUpdateTaskId = -1;
	public static final Set<UUID> activeHighlightUsers = new HashSet<UUID>();;

	private void registerCommands() {
		this.getCommand("framehighlight").setExecutor(new FrameHighlight(this));
		this.getCommand("frameshowhide").setExecutor(new FrameShowHide(this));
	}

	@Override
	public void onEnable() {
		this.registerCommands();
		frameDestroyListener = new FrameDestroyListener();
		this.getServer().getPluginManager().registerEvents(frameDestroyListener, this);
		// Start Active player update
		activePlayerUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ActivePlayerUpdate(this), 0, 10);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		HandlerList.unregisterAll(frameDestroyListener);
		if (activePlayerUpdateTaskId != -1) {
			Bukkit.getScheduler().cancelTask(activePlayerUpdateTaskId);
		}
	}
}
