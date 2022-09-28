package me.cth451.paperframe.util;

public class HighlightOptions {
	/**
	 * whether the player requested to highlight hidden frames only
	 */
	public boolean hiddenOnly;
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

	public HighlightOptions(boolean hiddenOnly, double range) {
		this.hiddenOnly = hiddenOnly;
		this.range = range;
	}
}
