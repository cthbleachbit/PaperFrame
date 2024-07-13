package me.cth451.paperframe.util.exceptions;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Drawing;
import me.cth451.paperframe.util.Targeting;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Exception thrown when more than 1 or 0 frame are stacked in the same spot.
 * <p>
 * This exception is used by these targeting routine(s):
 * <ul>
 *     <li>{@link Targeting#byRectangleTopLeftCorner(Block, BlockFace, int, int)} </li>
 * </ul>
 */
public class InvalidFrameCountException extends RuntimeException {
	public static record BadFrameCount(@NotNull Block block, @NotNull BlockFace face, int frameCount) {
	}

	/**
	 * Content of exception
	 */
	private final LinkedList<BadFrameCount> badFaces = new LinkedList<>();

	InvalidFrameCountException(Collection<BadFrameCount> badFaces) {
		this.badFaces.addAll(badFaces);
	}


	@Override
	public String getMessage() {
		return String.format("%d blocks has more than 1 frames or no frames within specified geometry",
		                     this.badFaces.size());
	}

	public void scheduleStickyDraw(PaperFramePlugin plugin) {
		this.badFaces.forEach((bf) -> {
			Particle.DustOptions drawOptions = new Particle.DustOptions(bf.frameCount > 1 ? Color.WHITE : Color.RED,
			                                                            1.0f);
			Drawing.scheduleStickyDraw(plugin, () -> Drawing.drawBoundingBox(bf.block, drawOptions), 5, 10);
		});
	}
}
