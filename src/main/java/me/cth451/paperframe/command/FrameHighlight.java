package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.HighlightOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

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
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		HighlightOptions options = new HighlightOptions(false, this.plugin.getConfig().getDouble(
				"commands.framehighlight.default_radius", HighlightOptions.DEFAULT_RADIUS));
		Iterator<String> itr = Arrays.stream(argv1).iterator();
		while (itr.hasNext()) {
			String arg = itr.next();
			if (arg.equals("-h")) {
				options.hiddenOnly = true;
			} else if (arg.equals("-r")) {
				if (itr.hasNext()) {
					try {
						options.range = Double.parseDouble(itr.next());
					} catch (NumberFormatException e) {
						player.sendMessage("Invalid radius specified after -r " + e.getMessage());
						return false;
					}
				} else {
					player.sendMessage("No radius specified after -r");
					return false;
				}
			} else if (arg.startsWith("-r")) {
				try {
					options.range = Double.parseDouble(arg.substring(2));
				} catch (NumberFormatException e) {
					player.sendMessage("Invalid radius specified after -r");
					return false;
				}
			}
		}

		if (options.range > this.plugin.getConfig()
		                               .getDouble("commands.framehighlight.max_radius", HighlightOptions.MAX_RADIUS)) {
			options.range = this.plugin.getConfig()
			                           .getDouble("commands.framehighlight.max_radius", HighlightOptions.MAX_RADIUS);
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
			player.sendMessage(
					String.format("Highlighting %s frames within %s radius",
					              options.hiddenOnly ? "hidden" : "all",
					              options.range));
			this.plugin.startPlayerUpdate();
		} else {
			player.sendMessage("Item frame highlighting disabled");
		}

		return true;
	}
}
