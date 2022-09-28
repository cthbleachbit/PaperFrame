package me.cth451.paperframe;

import me.cth451.paperframe.command.*;
import me.cth451.paperframe.listener.*;
import me.cth451.paperframe.task.*;
import me.cth451.paperframe.util.HighlightOptions;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class PaperFramePlugin extends JavaPlugin {
	private FrameDestroyListener frameDestroyListener = null;

	// protect access to activeUpdateTask
	private final ReentrantLock activeUpdateTaskLock = new ReentrantLock();
	private int activeUpdateTask = -1;
	public static final HashMap<UUID, HighlightOptions> activeHighlightUsers = new HashMap<>();
	;

	private void registerCommands() {
		this.getCommand("framehighlight").setExecutor(new FrameHighlight(this));
		this.getCommand("frameshowhide").setExecutor(new FrameShowHide(this));
		this.getCommand("frameconfigreload").setExecutor(new FrameConfigReload(this));
	}

	public void startPlayerUpdate() {
		// Start Active player update
		boolean started = false;
		{
			activeUpdateTaskLock.lock();
			if (activeUpdateTask == -1) {
				activeUpdateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new FrameHighlightTask(this),
				                                                                   0, 13);
				started = true;
			}
			activeUpdateTaskLock.unlock();
		}
		if (started) {
			Bukkit.getConsoleSender().sendMessage("[PaperFrame] Highlighting update task started");
		}
	}

	public void stopPlayerUpdate() {
		boolean stopped = false;
		{
			activeUpdateTaskLock.lock();
			if (activeUpdateTask > 0) {
				Bukkit.getScheduler().cancelTask(activeUpdateTask);
				activeUpdateTask = -1;
				stopped = true;
			}
			activeUpdateTaskLock.unlock();
		}
		if (stopped) {
			Bukkit.getConsoleSender().sendMessage("[PaperFrame] Highlighting update task stopped");
		}
	}

	@Override
	public void onEnable() {
		this.registerCommands();
		this.saveDefaultConfig();
		frameDestroyListener = new FrameDestroyListener();
		this.getServer().getPluginManager().registerEvents(frameDestroyListener, this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		HandlerList.unregisterAll(frameDestroyListener);
		if (activeUpdateTask != -1) {
			Bukkit.getScheduler().cancelTask(activeUpdateTask);
		}
	}
}
