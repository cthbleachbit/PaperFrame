package me.cth451.paperframe.command.base;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.getopt.ArgvParser;
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

import java.util.HashMap;
import java.util.List;

import static me.cth451.paperframe.util.Targeting.findFrameByTargetedEntity;

/**
 * An abstract class defining a toggle switch on a frame property.
 * <p>
 *     When used without argument this command acts as a toggle switch.
 * <p>
 *     When used with `-1` or `-0` this command turns the property on or off.
 */
public abstract class ToggleCommandExecutor implements CommandExecutor {
	protected final PaperFramePlugin plugin;

	protected final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("on", '1', UnixFlagSpec.FlagType.EXIST, "on"),
			new UnixFlagSpec("off", '0', UnixFlagSpec.FlagType.EXIST, "off"),
	};

	protected final static ArgvParser argvParser = new ArgvParser(List.of(arguments));

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
	 * @param frame    frame checked
	 * @param newState new state
	 * @return a formatted string on the new state on change
	 */
	protected abstract String fmtStatusChanged(@NotNull ItemFrame frame, boolean newState);

	/**
	 * Message when no change is needed (i,e, turning on the switch when the switch is already on)
	 *
	 * @param frame        frame checked
	 * @param currentState current state
	 * @return a formatted string when no change is needed
	 */
	protected abstract String fmtNoChangeNeeded(@NotNull ItemFrame frame, boolean currentState);

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}
		HashMap<String, Object> parsed;
		try {
			parsed = argvParser.parse(List.of(argv1));
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}
		// Check whether the player is looking at an item frame
		ItemFrame frame = findFrameByTargetedEntity(player);

		if (frame == null) {
			// No item frames in range
			player.sendMessage("Can't find an item frame where you are looking at");
			return true;
		}

		/* Verify action */
		boolean toggle = false;
		if ((boolean) parsed.get("on") && (boolean) parsed.get("off")) {
			player.sendMessage(ChatColor.RED + "Must specify only one of --on or --off");
			return false;
		} else if (!((boolean) parsed.get("on") || (boolean) parsed.get("off"))) {
			toggle = true;
		}

		/* Whether the property should be enabled */
		boolean enabling = toggle ? !getter(frame) : (boolean) parsed.get("on");
		boolean changed = false;

		if (getter(frame) != enabling) {
			setter(frame, enabling, player);
			changed = true;
		}

		final Particle.DustOptions options = new Particle.DustOptions(enabling ? Color.GREEN : Color.RED, 1.0f);
		player.sendMessage(
				ChatColor.GREEN + (changed ? fmtStatusChanged(frame, enabling) : fmtNoChangeNeeded(frame, enabling)));
		Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 3, 10);
		return true;
	}
}
