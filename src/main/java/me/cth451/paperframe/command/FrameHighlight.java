package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.HighlightOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * highlight the frames in range even when they are hidden, and turn off in 5 seconds
 */
public class FrameHighlight implements CommandExecutor {
	private final PaperFramePlugin plugin;

	public FrameHighlight(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			return false;
		}

		HighlightOptions options = new HighlightOptions(false, HighlightOptions.HIGHLIGHT_RANGE);
		for (String arg : argv1) {
			if (arg.equals("-h")) {
				options.hiddenOnly = true;
			}
		}
		boolean enabling;
		synchronized (PaperFramePlugin.activeHighlightUsers) {
			if (PaperFramePlugin.activeHighlightUsers.containsKey(player.getUniqueId())) {
				PaperFramePlugin.activeHighlightUsers.remove(player.getUniqueId());
				enabling = false;
			} else {
				PaperFramePlugin.activeHighlightUsers.put(player.getUniqueId(), options);
				enabling = true;
			}
		}

		if (enabling) {
			if (options.hiddenOnly) {
				player.sendMessage("Item frame highlighting enabled - hidden frames only");
			} else {
				player.sendMessage("Item frame highlighting enabled");
			}
		} else {
			player.sendMessage("Item frame highlighting disabled");
		}

		return true;
	}
}
