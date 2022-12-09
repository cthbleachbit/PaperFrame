package me.cth451.paperframe.util;

import org.bukkit.entity.ItemFrame;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class HighlightOptions implements Predicate<ItemFrame> {

	/**
	 * A list of filters to be applied on top of each other. An empty list is equivalent to a pass-all filter.
	 */
	public List<FrameFilter> filters;

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

	/**
	 * Construct highlight options with selection range only
	 * @param range selection range
	 */
	public HighlightOptions(double range) {
		this.filters = new LinkedList<>();
		this.range = range;
	}

	/**
	 * Construct highlight options with selection range and a list of filters
	 * @param filters the list of filters to apply
	 * @param range selection range
	 */
	public HighlightOptions(List<FrameFilter> filters, double range) {
		this.filters = filters;
		this.range = range;
	}

	/**
	 * Apply the list of filters to a candidate item frame.
	 *
	 * @param frame item frame to check
	 * @return whether this frame satisfies the criteria of the filter
	 */
	@Override
	public boolean test(ItemFrame frame) {
		return this.filters.stream().map(f -> f.test(frame)).reduce((a, b) -> a && b).orElse(true);
	}

	@Override
	public String toString() {
		String filterDesc =
				filters.isEmpty()
						? "none"
						: String.join(", ",
						              filters.stream().map(FrameFilter::toString)
						                     .toList());
		return String.format("Highlighting within %f blocks with filter %s", range, filterDesc);
	}
}
