package me.cth451.paperframe.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FrameShowHide implements CommandExecutor {
	public static final int SELECTION_RANGE = 6;
	private final PaperFramePlugin plugin;

	public FrameShowHide(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Find ItemFrame by minimizing angle difference
	 *
	 * @param player player requesting this search
	 * @return the ItemFrame cloest to the player crosshair, or null if nothing is in range
	 */
	private ItemFrame findFrameByAngle(@NotNull Player player) {
		ItemFrame frame = null;
		float bestAngle = 0.2f;

		Vector playerLookDir = player.getEyeLocation().getDirection();
		Vector playerEyeLoc = player.getEyeLocation().toVector();

		for (Entity entity : player.getNearbyEntities(SELECTION_RANGE, SELECTION_RANGE, SELECTION_RANGE)) {
			if (!(entity instanceof ItemFrame)) {
				continue;
			}
			Vector frameLoc = entity.getLocation().toVector();
			Vector playerEntityVec = frameLoc.subtract(playerEyeLoc);
			float angle = playerLookDir.angle(playerEntityVec);

			if (bestAngle > angle) {
				frame = (ItemFrame) entity;
				bestAngle = angle;
			}
		}

		return frame;
	}

	private ItemFrame findFrameByTargetedEntity(@NotNull Player player) {
		Entity entity = player.getTargetEntity(SELECTION_RANGE);
		if (entity == null) {
			return null;
		}

		if (!(entity instanceof ItemFrame frame)) {
			return null;
		}

		return frame;
	}

	private ItemFrame findFrameByTargetedBlock(@NotNull Player player) {
		TargetBlockInfo targetedBlock = player.getTargetBlockInfo(SELECTION_RANGE, TargetBlockInfo.FluidMode.NEVER);
		if (targetedBlock == null || targetedBlock.getBlock().isEmpty()) {
			return null;
		}
		Block frontBlock = targetedBlock.getBlock().getRelative(targetedBlock.getBlockFace());
		BoundingBox box = new BoundingBox(frontBlock.getX(), frontBlock.getY(), frontBlock.getZ(),
		                                  frontBlock.getX() + 1, frontBlock.getY() + 1, frontBlock.getZ() + 1);


		for (Entity entity : player.getNearbyEntities(SELECTION_RANGE, SELECTION_RANGE, SELECTION_RANGE)) {
			if (!(entity instanceof ItemFrame frame)) {
				continue;
			}
			if (box.contains(frame.getLocation().toVector()) && frame.getAttachedFace()
			                                                         .getOppositeFace() == targetedBlock.getBlockFace()) {
				return frame;
			}
		}
		Runnable drawCall = () -> Drawing.drawBoundingBox(box, player.getWorld(),
		                                                  new Particle.DustOptions(Color.WHITE, 1.0f));
		Drawing.scheduleStickyDraw(this.plugin, drawCall, 5, 10);
		return null;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if (!(commandSender instanceof Player player)) {
			return false;
		}

		// Check whether the player is looking at an item frame
		ItemFrame frame = findFrameByTargetedBlock(player);

		if (frame == null) {
			// No item frames in range
			player.sendMessage("Can't find an item frame where you are looking");
		} else {
			boolean isVisible = frame.isVisible();
			frame.setVisible(!isVisible);
			final Particle.DustOptions options = new Particle.DustOptions(isVisible ? Color.RED : Color.GREEN, 1.0f);
			player.sendMessage(isVisible ? "Frame hidden" : "Frame revealed");
			Drawing.scheduleStickyDraw(this.plugin, () -> Drawing.drawBoundingBox(frame, options), 3, 10);
		}
		return true;
	}
}
