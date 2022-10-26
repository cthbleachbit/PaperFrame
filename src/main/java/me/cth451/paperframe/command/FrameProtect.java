package me.cth451.paperframe.command;

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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static me.cth451.paperframe.util.FrameProperties.*;
import static me.cth451.paperframe.util.Targeting.findFrameByTargetedEntity;

/**
 * Toggle protected status on an item frame. A protected frame:
 * <ul>
 *     <li>
 *         Cannot have its contents replaced or rotated (i.e. on player right click).
 *     </li>
 *     <li>
 *         Cannot be destroyed by taking damage from entity (player damage, explosions, etc).
 *     </li>
 *     <li>
 *         Cannot be destroyed by removing the supporting block or placing a block in the occupying space.
 *     </li>
 * </ul>
 * The events are caught and cancelled by {@link me.cth451.paperframe.eventlistener.FrameProtectListener}.
 * <p>
 * Note: vanilla minecraft allows to set "Fixed"  or "Invulnerable" tag on item frames. These tags fail to prevent
 * players in creative mode from destroying / modifying an item frames. `/protect -1` only sets "Fixed" tag as an
 * indication that someone has set protection status for this item frame.
 */
public class FrameProtect implements CommandExecutor {
	private final PaperFramePlugin plugin;

	private final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("on", '1', UnixFlagSpec.FlagType.EXIST, "on"),
			new UnixFlagSpec("off", '0', UnixFlagSpec.FlagType.EXIST, "off"),
	};

	private final static ArgvParser argvParser = new ArgvParser(List.of(arguments));

	public FrameProtect(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

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

		if ((boolean) parsed.get("on") && (boolean) parsed.get("off")) {
			player.sendMessage(ChatColor.RED + "Must specify either --on or --off");
			return false;
		} else if (!((boolean) parsed.get("on") || (boolean) parsed.get("off"))) {
			player.sendMessage("Frame is " + (frame.isFixed() ? "protected by " + getProtectedBy(
					frame) + " at " + getProtectedAt(frame) : "unprotected"));
			return true;
		}

		// True if protecting, False if un-protecting
		boolean protecting = (boolean) parsed.get("on");
		boolean changed = false;

		if (frame.isFixed() != protecting) {
			frame.setFixed(protecting);
			setProtectedBy(frame, protecting ? player.getName() : null);
			setProtectedAt(frame, protecting ? new Date() : null);
			changed = true;
		}

		final Particle.DustOptions options = new Particle.DustOptions(protecting ? Color.GREEN : Color.RED, 1.0f);
		player.sendMessage(
				ChatColor.GREEN + "Frame"
						+ (changed ? " " : " is already ")
						+ (protecting ? "protected" : "unprotected")
						+ (((!changed) && frame.isFixed()) ?
						(" by " + getProtectedBy(frame) + " at " + getProtectedAt(frame)) : ""));
		Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 3, 10);
		return true;
	}
}
