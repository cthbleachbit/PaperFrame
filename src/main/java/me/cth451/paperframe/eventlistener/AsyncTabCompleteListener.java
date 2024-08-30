package me.cth451.paperframe.eventlistener;

import java.util.List;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.collect.Iterables;
import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.command.base.IAsyncTabCompleteExecutor;
import me.cth451.paperframe.util.getopt.Unescape;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AsyncTabCompleteListener implements Listener {

	private final PaperFramePlugin plugin;

	public AsyncTabCompleteListener(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
		/* See if this completion is already handled or has results */
		if (!event.isCommand() || event.isHandled() || !event.getCompletions().isEmpty()) {
			return;
		}

		/* Tokenize */
		CommandSender sender = event.getSender();
		List<String> argv0p = Unescape.tokenize(event.getBuffer());
		if (argv0p.isEmpty()) {
			return;
		}

		String argv0woSlash = StringUtils.stripStart(argv0p.get(0), "/");
		if (argv0woSlash.isEmpty()) {
			return;
		}

		/* See if this command is ours to handle */
		PluginCommand command = this.plugin.getCommand(argv0woSlash);
		if (command == null) {
			return;
		}


		CommandExecutor executor = command.getExecutor();
		if (!(executor instanceof IAsyncTabCompleteExecutor completionProvider)) {
			/* This command does not support completion */
			return;
		}

		/* Last token empty = all tokens complete - only need to complete future args */
		boolean lastArgComplete = Iterables.getLast(argv0p).isEmpty();
		this.plugin.getComponentLogger().info("Completing: {}", event.getBuffer());

		List<String> parsableArguments = argv0p.subList(1, argv0p.size() - (lastArgComplete ? 1 : 0));
		List<AsyncTabCompleteEvent.Completion> completions =
				completionProvider.onTabComplete(sender, parsableArguments, lastArgComplete);

		event.setHandled(true);
		event.completions().clear();
		event.completions().addAll(completions);
	}
}
