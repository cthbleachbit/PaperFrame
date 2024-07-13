package me.cth451.paperframe.util.exceptions;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * Builder class for {@link InvalidFrameCountException}
 */
public class InvalidFrameCountExceptionBuilder {
	private final LinkedList<InvalidFrameCountException.BadFrameCount> badFrameCounts = new LinkedList<>();

	public InvalidFrameCountExceptionBuilder() {

	}

	public void addBadFace(@NotNull Block block, @NotNull BlockFace face, int frame_count) {
		badFrameCounts.add(new InvalidFrameCountException.BadFrameCount(block, face, frame_count));
	}

	public void verifyOrThrow() {
		if (!badFrameCounts.isEmpty()){
			throw new InvalidFrameCountException(badFrameCounts);
		}
	}
}
