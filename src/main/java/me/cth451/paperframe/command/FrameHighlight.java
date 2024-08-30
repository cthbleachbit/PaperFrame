package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.FrameFilter;
import me.cth451.paperframe.util.HighlightOptions;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.ParameterRequiredException;
import me.cth451.paperframe.util.getopt.PrintHelpException;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static me.cth451.paperframe.util.FrameFilter.*;

/**
 * highlight the frames in range
 */
public class FrameHighlight implements CommandExecutor {
	private final PaperFramePlugin plugin;

	private final static UnixFlagSpec[] arguments = {
			/* Show hidden frames only */
			HIDDEN.toFlagSpec(),
			/* Show protected frames only */
			PROTECTED.toFlagSpec(),
			/* Show frames that are overlapping with other frames */
			STACKED.toFlagSpec(),
			/* Show frames that are empty only */
			EMPTY.toFlagSpec(),
			/* Radius in blocks which to find frames in */
			new UnixFlagSpec("radius", 'r', UnixFlagSpec.FlagType.PARAMETRIZE, "radius", Double::parseDouble),
			/* Whether selection should be made from WorldEdit cuboid */
			new UnixFlagSpec("use-we", 'w', UnixFlagSpec.FlagType.EXIST, "use-we"),
	};

	private final static ArgvParser argvParser = new ArgvParser(Arrays.asList(arguments));

	public FrameHighlight(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Toggle highlight status of item frames near you. Multiple filters may be stacked together.
	 * <ul>
	 * <li>-h = hidden ones only</li>
	 * <li>-p = protected ones only</li>
	 * <li>-s = overlapping / stacked ones only</li>
	 * <li>-e = empty frames only</li>
	 * <li>-r N = highlight within radius N</li>
	 * </ul>
	 *
	 * @param commandSender Source of the command
	 * @param command       Command which was executed
	 * @param argv0         Alias of the command which was used
	 * @param argv1         Passed command arguments
	 * @return whether the invocation is sound
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		HighlightOptions options = new HighlightOptions(this.plugin.getConfig().getDouble(
				"commands.framehighlight.default_radius", HighlightOptions.DEFAULT_RADIUS));

		HashMap<String, Object> parsed;
		try {
			parsed = argvParser.parse(List.of(argv1));
		} catch (IllegalArgumentException | PrintHelpException | ParameterRequiredException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			player.sendMessage(ChatColor.YELLOW + command.getDescription());
			player.sendMessage(command.getUsage());
			return true;
		}

		if (parsed.containsKey("radius")) {
			options.range = (double) parsed.get("radius");
		}

		if ((boolean) parsed.get("use-we")) {
			if (!this.plugin.getDependencyManager().isWorldEditAvailable(player)) {
				return true;
			}
			options.worldedit = true;
		}

		for (FrameFilter f : FrameFilter.values()) {
			if (parsed.containsKey(f.toString())) {
				if ((boolean) parsed.get(f.toString()))
					options.filters.add(f);
			} else {
				plugin.getLogger().severe(String.format("Filter %s doesn't exist?! Please submit a bug report!", f));
			}
		}

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
			player.sendMessage(ChatColor.GREEN + options.toString());
			this.plugin.startPlayerUpdate();
		} else {
			player.sendMessage(ChatColor.GREEN + "Item frame highlighting disabled");
		}

		return true;
	}
}
