package me.cth451.paperframe.util;

public class HighlightOptions {
	public boolean hiddenOnly;
	public double range;

	public static final int HIGHLIGHT_RANGE = 10;

	public HighlightOptions(boolean hiddenOnly, double range) {
		this.hiddenOnly = hiddenOnly;
		this.range = range;
	}
}
