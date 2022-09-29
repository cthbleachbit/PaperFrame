package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.HighlightOptions;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;

/**
 * highlight the frames in range even when they are hidden, and turn off in 5 seconds
 */
public class FrameHighlight implements CommandExecutor {
	private final PaperFramePlugin plugin;

	private final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("hidden", 'h', UnixFlagSpec.FlagType.EXIST, "hidden"),
			new UnixFlagSpec("radius", 'r', UnixFlagSpec.FlagType.PARAMETRIZE, "radius", Double::parseDouble),
	};

	private final ArgvParser argvParser;

	public FrameHighlight(PaperFramePlugin plugin) {
		this.plugin = plugin;
		argvParser = new ArgvParser(Arrays.asList(arguments));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		HighlightOptions options = new HighlightOptions(false, this.plugin.getConfig().getDouble(
				"commands.framehighlight.default_radius", HighlightOptions.DEFAULT_RADIUS));

		HashMap<String, Object> parsed;
		try {
			parsed = argvParser.parse(argv1);
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}

		if (parsed.containsKey("radius")) {
			options.range = (double) parsed.get("radius");
		}
		options.hiddenOnly = (boolean) parsed.get("hidden");

		double confMaxRadius = this.plugin.getConfig()
		                                  .getDouble("commands.framehighlight.max_radius", HighlightOptions.MAX_RADIUS);
		if (options.range > confMaxRadius) {
			options.range = confMaxRadius;
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
			String enablingMessage = String.format("Highlighting %s frames within %s radius",
			                                       options.hiddenOnly ? "hidden" : "all",
			                                       options.range);
			player.sendMessage(ChatColor.GREEN + enablingMessage);
			this.plugin.startPlayerUpdate();
		} else {
			player.sendMessage(ChatColor.GREEN + "Item frame highlighting disabled");
		}

		return true;
	}
}
