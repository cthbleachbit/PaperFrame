package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.command.base.ToggleCommandExecutor;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;

import static me.cth451.paperframe.util.FrameProperties.*;

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
public class FrameProtect extends ToggleCommandExecutor {
	public FrameProtect(PaperFramePlugin plugin) {
		super(plugin);
	}

	/**
	 * Helper function to set protected status and acting player for the change
	 *
	 * @param frame  frame to change
	 * @param active whether protection should be on
	 * @param player acting player
	 */
	public static void setProtectedByPlayer(@NotNull ItemFrame frame, boolean active, @NotNull Player player) {
		setProtected(frame, active);
		setProtectedBy(frame, active ? player.getName() : null);
		setProtectedAt(frame, active ? new Date() : null);
	}

	@Override
	protected void setter(@NotNull ItemFrame frame, boolean enable, @NotNull Player player) {
		setProtectedByPlayer(frame, enable, player);
	}

	@Override
	protected boolean getter(@NotNull ItemFrame frame) {
		return getProtected(frame);
	}

	@Override
	protected String actionToString(Action action) {
		return switch (action) {
			case TOGGLE -> "Toggled protection for";
			case ENABLE -> "Protected";
			case DISABLE -> "Removed protection for";
		};
	}

	@Override
	protected String fmtStatusChanged(@NotNull Collection<ItemFrame> changeset, Action action) {
		if (changeset.isEmpty()) {
			return "No changes needed.";
		}
		return String.format("%s %d frames.", actionToString(action), changeset.size());
	}
}
