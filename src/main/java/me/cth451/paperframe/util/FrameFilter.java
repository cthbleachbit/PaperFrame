package me.cth451.paperframe.util;

import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;

import java.util.function.Predicate;

/**
 * Highlight filtering methods available
 */
public enum FrameFilter implements Predicate<ItemFrame> {
	/**
	 * retain hidden frames only
	 */
	HIDDEN,
	/**
	 * retain protected item frames only
	 */
	PROTECTED,
	/**
	 * retain frames that are stacked on top of other frames only
	 */
	STACKED,
	/**
	 * retain item frames that doesn't have anything in it
	 */
	EMPTY,
	;

	/**
	 * @return human readable description
	 */
	public String toString() {
		return name().toLowerCase();
	}

	/**
	 * Apply filter to a candidate item frame
	 *
	 * @param frame item frame to check
	 * @return whether this frame satisfies the criteria of the filter
	 */
	@Override
	public boolean test(ItemFrame frame) {
		return switch (this) {
			case HIDDEN -> FrameProperties.getHidden(frame);
			case PROTECTED -> FrameProperties.getProtected(frame);
			case STACKED -> {
				World world = frame.getWorld();
				Location loc = frame.getLocation();
				/* Use a ridiculously small selection range */
				yield world.getNearbyEntitiesByType(ItemFrame.class, loc, 0.015625d).size() > 1L;
			}
			case EMPTY -> frame.getItem().getType() == Material.AIR || frame.getItem().getAmount() == 0;
		};
	}

	/**
	 * Return flag specification for this filter for use in /framehighlight command
	 *
	 * @return command argument specification for the filter
	 */
	public UnixFlagSpec toFlagSpec() {
		String name = toString();
		return new UnixFlagSpec(name, name.charAt(0), UnixFlagSpec.FlagType.EXIST, name);
	}
}
