package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.FrameProperties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.cth451.paperframe.util.Targeting.findFrameByTargetedEntity;

public class FrameStat implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	public FrameStat(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		// Check whether the player is looking at an item frame
		ItemFrame frame = findFrameByTargetedEntity(player);

		if (frame == null) {
			// No item frames in range
			player.sendMessage("Can't find an item frame where you are looking at");
		} else {
			boolean isHidden = !frame.isVisible();
			boolean isProtected = frame.isInvulnerable();
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append(String.format("%s at coordinates %s:\n",
			                                    frame instanceof GlowItemFrame ? "Glowing item frame" : "Item frame",
			                                    frame.getLocation().toVector()));
			messageBuilder.append(ChatColor.RESET)
			              .append(" Hidden: ");
			if (isHidden) {
				messageBuilder.append(ChatColor.RED)
				              .append(String.format("☑ (%s on %s)\n",
				                                    FrameProperties.getHiddenBy(frame),
				                                    FrameProperties.getHiddenAt(frame)));
			} else {
				messageBuilder.append(ChatColor.GREEN)
				              .append("☒ Visible\n");
			}

			messageBuilder.append(ChatColor.RESET)
			              .append(" Protected: ");
			if (isProtected) {
				messageBuilder.append(ChatColor.RED)
				              .append(String.format("☑ (%s on %s)\n",
				                                    FrameProperties.getProtectedBy(frame),
				                                    FrameProperties.getProtectedAt(frame)));
			} else {
				messageBuilder.append(ChatColor.GREEN)
				              .append("☒ Not protected\n");
			}
			player.sendMessage(messageBuilder.toString());
		}
		return true;
	}
}
