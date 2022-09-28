package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Reloads plugin configuration
 */
public class FrameConfigReload implements CommandExecutor {
	private final PaperFramePlugin plugin;

	public FrameConfigReload(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		this.plugin.reloadConfig();
		commandSender.sendMessage("Config Reloaded!");
		commandSender.sendMessage(String.format("commands.framehighlight.max_radius: %f", this.plugin.getConfig().getDouble("commands.framehighlight.max_radius")));
		commandSender.sendMessage(String.format("commands.framehighlight.default_radius: %f", this.plugin.getConfig().getDouble("commands.framehighlight.default_radius")));
		return true;
	}
}