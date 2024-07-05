package me.cth451.paperframe.command.base;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.Targeting;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.PrintHelpException;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.cth451.paperframe.util.Targeting.byTargetedEntity;

/**
 * An abstract class defining a toggle switch on a frame property.
 * <p>
 * When used without argument this command acts as a toggle switch.
 * <p>
 * When used with `-1` or `-0` this command turns the property on or off.
 * <p>
 * `-w` causes the command will act on all frames within WorldEdit selection range instead of the frame under the
 * cursor. WorldEdit must be active on the server. Otherwise, the command will fail with an error message in the chat.
 */
public abstract class ToggleCommandExecutor implements CommandExecutor {
	protected final PaperFramePlugin plugin;

	protected final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("on", '1', UnixFlagSpec.FlagType.EXIST, "on"),
			new UnixFlagSpec("off", '0', UnixFlagSpec.FlagType.EXIST, "off"),
			new UnixFlagSpec("use-we", 'w', UnixFlagSpec.FlagType.EXIST, "use-we"),
	};

	protected final static ArgvParser argvParser = new ArgvParser(List.of(arguments));

	/**
	 * Action specified by the user
	 */
	protected enum Action {
		TOGGLE,
		ENABLE,
		DISABLE,
	}

	/**
	 * Convert an action to command specific "verb"
	 *
	 * @param action player action
	 * @return appropriate description for the property controlled by the command
	 */
	protected abstract String actionToString(Action action);

	/**
	 * Default constructor
	 *
	 * @param plugin plugin instance
	 */
	protected ToggleCommandExecutor(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Subclass should override. Routine to get the property.
	 *
	 * @param frame frame to check
	 * @return property boolean value
	 */
	protected abstract boolean getter(@NotNull ItemFrame frame);

	/**
	 * Subclass should override. Routine to set the property.
	 *
	 * @param frame  frame to check
	 * @param enable whether the property should be enabled
	 * @param player acting player
	 */
	protected abstract void setter(@NotNull ItemFrame frame, boolean enable, @NotNull Player player);

	/**
	 * Apply an action on the frame
	 *
	 * @param frame  frame to change
	 * @param action action specified by the player
	 * @param player player executing the comand
	 * @return whether the item frame is changed
	 */
	private boolean update(@NotNull ItemFrame frame, Action action, @NotNull Player player) {
		/* Desired state */
		boolean desired;
		if (action == Action.TOGGLE) {
			desired = !getter(frame);
		} else {
			/* Uniformly turning on or turning off */
			desired = action == Action.ENABLE;
			if (desired == getter(frame)) {
				/* Early return when no change is needed */
				return false;
			}
		}

		setter(frame, desired, player);

		final Particle.DustOptions options = new Particle.DustOptions(desired ? Color.GREEN : Color.RED, 1.0f);
		Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 3, 10);
		return true;
	}

	/**
	 * Generate a summary on actions performed
	 *
	 * @param changeset frames changed
	 * @param action    action requested by the user
	 * @return a formatted string on the new state on change
	 */
	protected abstract String fmtStatusChanged(@NotNull Collection<ItemFrame> changeset, Action action);

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}
		HashMap<String, Object> parsed;
		try {
			parsed = argvParser.parse(List.of(argv1));
		} catch (IllegalArgumentException | PrintHelpException e) {
			player.sendMessage(ChatColor.YELLOW + e.getMessage());
			player.sendMessage(ChatColor.YELLOW + command.getDescription());
			player.sendMessage(command.getUsage());
			return true;
		}

		List<ItemFrame> targets;

		if ((boolean) parsed.get("use-we")) {
			/* Call WorldEdit to fetch selection range */
			targets = Targeting.byWorldEditCuboid(player);
			if (targets == null) {
				return true;
			}
			if (targets.isEmpty()) {
				player.sendMessage("Can't find an item frame within selected cuboid");
				return true;
			}
		} else {
			/* Check whether the player is looking at an item frame */
			ItemFrame frame = byTargetedEntity(player);
			if (frame == null) {
				player.sendMessage("Can't find an item frame where you are looking at");
				return true;
			}
			targets = List.of(frame);
		}

		/* Verify action */
		final Action action;
		if ((boolean) parsed.get("on") && (boolean) parsed.get("off")) {
			player.sendMessage(ChatColor.RED + "Must specify only one of --on or --off");
			return false;
		} else if ((boolean) parsed.get("on")) {
			action = Action.ENABLE;
		} else if ((boolean) parsed.get("off")) {
			action = Action.DISABLE;
		} else {
			action = Action.TOGGLE;
		}

		List<ItemFrame> changed = targets.stream().filter(frame -> update(frame, action, player)).toList();

		player.sendMessage(ChatColor.GREEN + fmtStatusChanged(changed, action));

		return true;
	}
}
