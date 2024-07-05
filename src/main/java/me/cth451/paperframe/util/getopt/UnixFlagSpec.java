package me.cth451.paperframe.util.getopt;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Specification for a unix style switch
 *
 * @param longArg     the long argument without "--", for example "radius"
 * @param shortArg    shorthand for this argument in a single character without preceding "-", say 'r'
 * @param type        type of the switch, use: EXIST - to set destination variable to true when the flag exists
 *                    PARAMETRIZE - to set the destination variable from the following parameter
 * @param destination where the resulting parameter or boolean gets stored
 * @param transform   A transform function to apply before storing the parameter - validation can occur in this step
 *                    This is unused when type is EXIST.
 */
public record UnixFlagSpec(String longArg,
                           Character shortArg,
                           FlagType type,
                           @NotNull String destination,
                           Function<? super String, Object> transform) {
	public UnixFlagSpec {
		// At least one long arg and one short arg should be specified
		if (longArg == null && shortArg == null) {
			throw new IllegalArgumentException("LongArg and ShortArg are both null");
		}
		Objects.requireNonNull(destination);
		Objects.requireNonNull(transform);
	}

	public UnixFlagSpec(String longArg,
	                    Character shortArg,
	                    FlagType type,
	                    @NotNull String destination) {
		this(longArg, shortArg, type, destination, Function.identity());
	}

	@Override
	public int hashCode() {
		return longArg.hashCode();
	}

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
}
