package me.cth451.paperframe;

import me.cth451.paperframe.command.FrameHighlight;
import me.cth451.paperframe.command.FrameShowHide;
import me.cth451.paperframe.listener.FrameDestroyListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PaperFramePlugin extends JavaPlugin {
	private FrameDestroyListener frameDestroyListener = null;
	public static final Set<UUID> activeUsers = new HashSet<UUID>();;
	public static final int SELECTION_RANGE = 5;

	private void registerCommands() {
		this.getCommand("framehighlight").setExecutor(new FrameHighlight(this));
		this.getCommand("frameshowhide").setExecutor(new FrameShowHide(this));
	}

	@Override
	public void onEnable() {
		this.registerCommands();
		frameDestroyListener = new FrameDestroyListener();
		this.getServer().getPluginManager().registerEvents(frameDestroyListener, this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		HandlerList.unregisterAll(frameDestroyListener);
	}
}
