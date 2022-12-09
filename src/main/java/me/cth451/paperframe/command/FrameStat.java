package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.FrameFilter;
import me.cth451.paperframe.util.FrameProperties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.cth451.paperframe.util.Targeting.findFrameByAttachedBlockFace;

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
		List<ItemFrame> frames = findFrameByAttachedBlockFace(player);

		if (frames.isEmpty()) {
			// No item frames in range
			player.sendMessage("Can't find an item frame where you are looking at");
			return true;
		}

		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(ChatColor.GREEN)
		              .append(String.format("Found %d item frames at %s\n", frames.size(),
		                                    frames.get(0).getLocation().toVector()));


		for (int i = 0; i < frames.size(); i++) {
			ItemFrame frame = frames.get(i);
			ItemStack content = frame.getItem();

			// Check properties
			boolean isHidden = !frame.isVisible();
			boolean isProtected = frame.isFixed();
			boolean isEmpty = FrameFilter.EMPTY.test(frame);
			boolean hasMeta = (isEmpty || content.hasItemMeta()) && content.getItemMeta() != null;

			String itemDesc = isEmpty ?
					"nothing" : String.format("%s x %d", content.getType(), content.getAmount());

			messageBuilder.append(ChatColor.RESET)
			              .append(String.format("[%d] %s with %s:\n",
			                                    i,
			                                    frame instanceof GlowItemFrame ? "Glowing item frame" : "Item frame",
			                                    itemDesc));

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

			if (hasMeta) {
				messageBuilder.append(ChatColor.RESET)
				              .append(" Item Metadata: ")
				              .append(content.getItemMeta())
				              .append("\n");
			}
		}

		player.sendMessage(messageBuilder.toString());
		return true;
	}
}
