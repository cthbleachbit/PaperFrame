package me.cth451.paperframe;

import me.cth451.paperframe.command.FrameConfigReload;
import me.cth451.paperframe.command.FrameHighlight;
import me.cth451.paperframe.command.FrameShowHide;
import me.cth451.paperframe.listener.FrameDestroyListener;
import me.cth451.paperframe.task.ActivePlayerUpdate;
import me.cth451.paperframe.util.HighlightOptions;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class PaperFramePlugin extends JavaPlugin {
	private FrameDestroyListener frameDestroyListener = null;
	private int activePlayerUpdateTaskId = -1;
	public static final HashMap<UUID, HighlightOptions> activeHighlightUsers = new HashMap<>();
	;

	private void registerCommands() {
		this.getCommand("framehighlight").setExecutor(new FrameHighlight(this));
		this.getCommand("frameshowhide").setExecutor(new FrameShowHide(this));
		this.getCommand("frameconfigreload").setExecutor(new FrameConfigReload(this));
	}

	@Override
	public void onEnable() {
		this.registerCommands();
		this.saveDefaultConfig();
		frameDestroyListener = new FrameDestroyListener();
		this.getServer().getPluginManager().registerEvents(frameDestroyListener, this);
		// Start Active player update
		activePlayerUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ActivePlayerUpdate(this),
		                                                                           0, 10);
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
