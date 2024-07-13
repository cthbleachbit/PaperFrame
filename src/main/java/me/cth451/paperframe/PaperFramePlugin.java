package me.cth451.paperframe;

import me.cth451.paperframe.command.*;
import me.cth451.paperframe.dependency.DependencyManager;
import me.cth451.paperframe.eventlistener.FrameProtectListener;
import me.cth451.paperframe.task.FrameHighlightTask;
import me.cth451.paperframe.util.HighlightOptions;
import me.cth451.paperframe.util.Targeting;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class PaperFramePlugin extends JavaPlugin {

	/* protect access to activeUpdateTask */
	private final ReentrantLock activeUpdateTaskLock = new ReentrantLock();
	private int activeUpdateTask = -1;
	public static final HashMap<UUID, HighlightOptions> activeHighlightUsers = new HashMap<>();

	/* Dependency manager */
	private DependencyManager dependencyManager = null;

	private void registerCommands() {
		Objects.requireNonNull(this.getCommand("frameprotect")).setExecutor(new FrameProtect(this));
		Objects.requireNonNull(this.getCommand("framehighlight")).setExecutor(new FrameHighlight(this));
		Objects.requireNonNull(this.getCommand("frameshowhide")).setExecutor(new FrameShowHide(this));
		Objects.requireNonNull(this.getCommand("frameconfigreload")).setExecutor(new FrameConfigReload(this));
		Objects.requireNonNull(this.getCommand("framestat")).setExecutor(new FrameStat(this));
		Objects.requireNonNull(this.getCommand("framemaps")).setExecutor(new FrameMapStack(this));
		Objects.requireNonNull(this.getCommand("frameunmap")).setExecutor(new FrameUnmap(this));
		Objects.requireNonNull(this.getCommand("frame2d")).setExecutor(new Frame2d(this));
	}

	private void registerEventListeners() {
		getServer().getPluginManager().registerEvents(new FrameProtectListener(this), this);
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
			getLogger().info("Frame Highlight Task started");
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
			getLogger().info("Frame Highlight Task stopped");
		}
	}

	public DependencyManager getDependencyManager() {
		return this.dependencyManager;
	}

	@Override
	public void onEnable() {
		this.registerCommands();
		this.registerEventListeners();
		this.saveDefaultConfig();
		this.dependencyManager = new DependencyManager(this);

		HighlightOptions.plugin = this;
		Targeting.plugin = this;
	}

	@Override
	public void onDisable() {
		this.stopPlayerUpdate();
	}
}
