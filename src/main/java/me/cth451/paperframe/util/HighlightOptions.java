package me.cth451.paperframe.util;

import org.bukkit.entity.ItemFrame;

import java.util.function.Predicate;

public class HighlightOptions {

	/**
	 * Highlight filtering methods available
	 */
	public enum HighlightFilter implements Predicate<ItemFrame> {
		/**
		 * Let all item frames through
		 */
		ALL,
		/**
		 * retain hidden frames only
		 */
		HIDDEN,
		/**
		 * retain protected item frames only
		 */
		PROTECTED;

		/**
		 * @return human readable description
		 */
		public String toString() {
			return switch (this) {
				case ALL -> "all";
				case HIDDEN -> "hidden";
				case PROTECTED -> "protected";
			};
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
				case ALL -> true;
				case HIDDEN -> !frame.isVisible();
				case PROTECTED -> frame.isInvulnerable();
			};
		}
	}

	/**
	 * Filter type
	 */
	public HighlightFilter filter;

	/**
	 * the range in which to find frames to highlight
	 */
	public double range;

	/**
	 * Default frame-finding radius - maybe overridden from configuration
	 */
	public static final double DEFAULT_RADIUS = 5;
	/**
	 * Maximum frame-finding radius - maybe overridden from configuration
	 */
	public static final double MAX_RADIUS = 10;

	public HighlightOptions(HighlightFilter filter, double range) {
		this.filter = filter;
		this.range = range;
	}
}
