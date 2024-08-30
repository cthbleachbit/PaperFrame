package me.cth451.paperframe.util.getopt;

public class ParameterRequiredException extends RuntimeException {
	private final UnixFlagSpec spec;
	private final boolean longArg;

	public ParameterRequiredException(UnixFlagSpec spec, boolean longArg) {
		this.spec = spec;
		this.longArg = longArg;
	}

	@Override
	public String getMessage() {
		return "Mandatory parameter for %s is missing".formatted(longArg ? spec.longArg() : spec.shortArg());
	}

	public UnixFlagSpec getSpec() {
		return this.spec;
	}
}
