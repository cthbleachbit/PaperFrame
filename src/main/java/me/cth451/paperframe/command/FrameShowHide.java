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
 * Reveal / Hide an item frame. This command make use of internal Visible tag. Enable state is hidden.
 */
public class FrameShowHide extends ToggleCommandExecutor {

	public FrameShowHide(PaperFramePlugin plugin) {
		super(plugin);
	}

	/**
	 * Helper function to set hidden status and acting player for the change
	 *
	 * @param frame  frame to change
	 * @param hidden whether the frame should be hidden
	 * @param player acting player
	 */
	public static void setShowHideByPlayer(@NotNull ItemFrame frame, boolean hidden, @NotNull Player player) {
		setHidden(frame, hidden);
		setHiddenBy(frame, hidden ? player.getName() : null);
		setHiddenAt(frame, hidden ? new Date() : null);
	}

	@Override
	protected void setter(@NotNull ItemFrame frame, boolean enable, @NotNull Player player) {
		setShowHideByPlayer(frame, enable, player);
	}

	@Override
	protected boolean getter(@NotNull ItemFrame frame) {
		return getHidden(frame);
	}

	@Override
	protected String actionToString(Action action) {
		return switch (action) {
			case TOGGLE -> "Toggled visibility for";
			case ENABLE -> "Hidden";
			case DISABLE -> "Revealed";
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
