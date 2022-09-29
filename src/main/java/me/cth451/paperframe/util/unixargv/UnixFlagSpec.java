package me.cth451.paperframe.util.unixargv;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Specification for a unix style switch
 *
 * @param longArg     the long argument without "--", for example "radius"
 * @param shortArg    optional shorthand for this argument in a single character without "-", say "r"
 * @param type        type of the switch, use EXIST to signify "flag exist" -> true relationship
 * @param destination where the resulting parameter or boolean gets stored
 */
public record UnixFlagSpec(@NotNull String longArg, Optional<Character> shortArg, FlagType type,
                           @NotNull String destination, Function<? super String, Object> transform) {
	public UnixFlagSpec {
		Objects.requireNonNull(longArg);
		Objects.requireNonNull(destination);
	}

	public UnixFlagSpec(@NotNull String longArg, Optional<Character> shortArg, FlagType type, @NotNull String destination) {
		this(longArg, shortArg, type, destination, Function.identity());
	}


	@Override
	public int hashCode() {
		return longArg.hashCode();
	}
}