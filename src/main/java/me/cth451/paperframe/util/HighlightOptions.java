package me.cth451.paperframe.util;

public class HighlightOptions {
	public boolean hiddenOnly;
	public double range;

	public static final double DEFAULT_RADIUS = 5;
	public static final double MAX_RADIUS = 10;

	public HighlightOptions(boolean hiddenOnly, double range) {
		this.hiddenOnly = hiddenOnly;
		this.range = range;
	}
}
