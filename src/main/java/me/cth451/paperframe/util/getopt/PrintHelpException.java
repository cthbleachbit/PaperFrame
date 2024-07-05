package me.cth451.paperframe.util.getopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrintHelpException extends RuntimeException {
	private final String message;

	PrintHelpException() {
		this(null);
	}

	PrintHelpException(@Nullable String message) {
		this.message = message;
	}

	@Override
	public @NotNull String getMessage() {
		return this.message == null ? "Command usage:" : this.message;
	}
}
