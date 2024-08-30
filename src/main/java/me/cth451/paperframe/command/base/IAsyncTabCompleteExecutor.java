package me.cth451.paperframe.command.base;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.collect.Iterables;
import me.cth451.paperframe.util.getopt.Unescape;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface that supports tab completion
 */
public interface IAsyncTabCompleteExecutor {

	/**
	 * Returns next step of action
	 *
	 * @param sender          command sender
	 * @param argv1p          argv1 and on without last trailing empty string
	 * @param lastArgComplete whether the last argument is complete (i.e. terminated by a space)
	 * @return a list of possible completions
	 */
	List<AsyncTabCompleteEvent.Completion> onTabComplete(CommandSender sender,
	                                                     List<String> argv1p,
	                                                     boolean lastArgComplete);

	/**
	 * Carries completion result
	 */
	record CompletionResult(String suggestion, Component tooltip) implements AsyncTabCompleteEvent.Completion {
		public CompletionResult(final @NotNull String suggestion, final @Nullable Component tooltip) {
			this.suggestion = suggestion;
			this.tooltip = tooltip;
		}

		@Override
		public @NotNull String suggestion() {
			return this.suggestion;
		}

		@Override
		public @Nullable Component tooltip() {
			return this.tooltip;
		}
	}

	/**
	 * Cuts off the escaped string as minecraft only consider texts after the last escaped-space for replacement:
	 * <br/>
	 * For a command like: <code>/f2d -n Arrival\ Boards/LB</code>: <br/>
	 * Completion will return <code>Arrival Boards/LBlue</code>. <br/>
	 * This function takes the string above and return <code>Boards/LBlue</code>
	 *
	 * @param string
	 * @return cut-off escaped completion string
	 */
	@Contract(pure = true)
	static @NotNull String escapeForCompletion(@NotNull String string) {
		if (string.isEmpty()) {
			return string;
		}
		String escaped = Unescape.escape(string);
		List<String> spaceSeparated = List.of(escaped.split(" ", -1));
		return Iterables.getLast(spaceSeparated);
	}
}
