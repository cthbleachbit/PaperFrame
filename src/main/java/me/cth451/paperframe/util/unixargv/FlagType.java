package me.cth451.paperframe.util.unixargv;

/**
 * Behavioral type for a switching flag
 */
public enum FlagType {
	/**
	 * Existence of a flag sets True in the named variable
	 */
	EXIST,
	/**
	 * The flag requires a parameter following the flag
	 */
	PARAMETRIZE,
}
