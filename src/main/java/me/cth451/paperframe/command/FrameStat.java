package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.FrameFilter;
import me.cth451.paperframe.util.FrameProperties;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.cth451.paperframe.util.Targeting.byTargetedStackedEntity;

public class FrameStat implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	public FrameStat(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Helper function to generate status text for one frame
	 *
	 * @param sb    output stream
	 * @param frame frame to check
	 */
	private void statsOnFrame(StringBuilder sb, ItemFrame frame) {
		ItemStack content = frame.getItem();

		// Check properties
		boolean isHidden = FrameProperties.getHidden(frame);
		boolean isProtected = FrameProperties.getProtected(frame);
		boolean isEmpty = FrameFilter.EMPTY.test(frame);
		boolean hasMeta = (isEmpty || content.hasItemMeta()) && content.getItemMeta() != null;

		String itemDesc = isEmpty ?
				"nothing" : String.format("%s x %d", content.getType(), content.getAmount());

		sb.append(ChatColor.RESET)
		  .append(String.format("%s with %s:\n",
		                        frame instanceof GlowItemFrame ? "Glowing item frame" : "Item frame",
		                        itemDesc));

		sb.append(ChatColor.RESET)
		  .append(" Hidden: ");

		if (isHidden) {
			sb.append(ChatColor.RED)
			  .append(String.format("☑ (%s on %s)\n",
			                        FrameProperties.getHiddenBy(frame),
			                        FrameProperties.getHiddenAt(frame)));
		} else {
			sb.append(ChatColor.GREEN)
			  .append("☒ Visible\n");
		}

		sb.append(ChatColor.RESET)
		  .append(" Protected: ");
		if (isProtected) {
			sb.append(ChatColor.RED)
			  .append(String.format("☑ (%s on %s)\n",
			                        FrameProperties.getProtectedBy(frame),
			                        FrameProperties.getProtectedAt(frame)));
		} else {
			sb.append(ChatColor.GREEN)
			  .append("☒ Not protected\n");
		}

		if (hasMeta) {
			sb.append(ChatColor.RESET)
			  .append(" Item Metadata: ")
			  .append(content.getItemMeta())
			  .append("\n");
		}
	}

	/**
	 * Helper function to generate status text for frames found attached to the same face on the same block
	 *
	 * @param sb     output stream
	 * @param frames frames to check
	 */
	private void statsOnFrames(StringBuilder sb, List<? extends ItemFrame> frames) {
		sb.append(ChatColor.GREEN)
		  .append(String.format("Found %d item frames at %s\n", frames.size(), frames.get(0).getLocation().toVector()));

		for (ItemFrame frame : frames) {
			statsOnFrame(sb, frame);
		}
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1) {
		if (!(commandSender instanceof Player player)) {
			commandSender.sendMessage("This command can only be used by a player");
			return false;
		}

		/* Check whether the player is looking at an item frame */
		List<ItemFrame> frameUnderCrossHair = byTargetedStackedEntity(player);
		StringBuilder messageBuilder = new StringBuilder();
		final Particle.DustOptions options = new Particle.DustOptions(Color.GREEN, 1.0f);

		if (!frameUnderCrossHair.isEmpty()) {
			/* No item frames on targeted block, check direct entity target instead */
			statsOnFrames(messageBuilder, frameUnderCrossHair);
			player.sendMessage(messageBuilder.toString());
			final ItemFrame sampleFrame = frameUnderCrossHair.get(0);
			Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(sampleFrame, options), 3, 10);
		} else {
			player.sendMessage("Can't find an item frame where you are looking at");
		}

		return true;
	}
}
