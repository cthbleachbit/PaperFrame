package me.cth451.paperframe.util.unixargv;

import org.jetbrains.annotations.Contract;

import java.util.*;

public class UnixArgv {
	private final Collection<UnixFlagSpec> arguments;

	/**
	 * Verify whether the arguments make sense:
	 * - check for conflicting long options
	 * - check for conflicting short hands
	 * - check for conflicting destination variable name
	 * This method only need to execute once during construction.
	 *
	 * @throws IllegalArgumentException if the argv specification violates the rules above
	 */
	private void verify() throws IllegalArgumentException {
		long distinctLongOpts =
				arguments.stream()
				         .map(UnixFlagSpec::longArg)
				         .distinct()
				         .count();
		if (distinctLongOpts != arguments.size()) {
			throw new IllegalArgumentException("Duplicated long argument names detected");
		}

		// count flags that has a shorthand defined
		long flagsWithShorthands =
				arguments.stream()
				         .filter((arg) -> arg.shortArg().isPresent())
				         .count();
		// count distinct shorthands used in the list
		long distinctShorthands =
				arguments.stream()
				         .filter((arg) -> arg.shortArg().isPresent())
				         .map((arg) -> arg.shortArg().get())
				         .distinct()
				         .count();
		if (flagsWithShorthands != distinctShorthands) {
			throw new IllegalArgumentException("Duplicated shorthands detected");
		}

		long distinctDestinations =
				arguments.stream()
				         .map(UnixFlagSpec::destination)
				         .distinct()
				         .count();
		if (distinctDestinations != arguments.size()) {
			throw new IllegalArgumentException("Duplicated destination variable names detected");
		}
	}

	/**
	 * Construct an argument parser based on the flag list provided
	 *
	 * @param arguments flag specification
	 * @throws IllegalArgumentException when flags are inconsistent
	 */
	public UnixArgv(Collection<UnixFlagSpec> arguments) throws IllegalArgumentException {
		this.arguments = arguments;
		verify();
	}

	/**
	 * Parse given list of argv1 and on into key-value pairs.
	 * If the type of {@link UnixFlagSpec} is EXACT, the resulting destination variable will contain True/False.
	 * If the type of {@link UnixFlagSpec} is PARAMETRIZE, the destination variable may
	 * - either do not exist in the mapping,
	 * - or maps to the parameter supplied to that flag as a String.
	 * This method does not modify internal states and should be safe to call concurrently.
	 *
	 * @param argv1p list of arguments from argv[1] and on
	 * @return mapping of parsed arguments and parameters
	 * @throws IllegalArgumentException if the input string array contains unrecognized options
	 */
	@Contract(pure = true)
	public HashMap<String, Object> parse(String[] argv1p) throws IllegalArgumentException {
		// Create defaulted existence flags
		HashMap<String, Object> ret = new HashMap<>();
		arguments.stream()
		         .filter((arg) -> arg.type() == FlagType.EXIST)
		         .forEach((arg) -> ret.put(arg.destination(), false));

		Iterator<String> itr = Arrays.stream(argv1p).iterator();

		while (itr.hasNext()) {
			String arg = itr.next();
			if (arg.startsWith("--")) {
				arg = arg.substring(2);
				String finalArg = arg;
				Optional<UnixFlagSpec> flagSpec = arguments.stream()
				                                           .filter((flag) -> flag.longArg().equals(finalArg))
				                                           .findFirst();
				if (flagSpec.isEmpty()) {
					throw new IllegalArgumentException("Unrecognized long option --" + arg);
				}

				switch (flagSpec.get().type()) {
					case EXIST -> ret.put(flagSpec.get().destination(), true);
					case PARAMETRIZE -> {
						if (itr.hasNext()) {
							arg = itr.next();
							Object transformArg;
							try {
								transformArg = flagSpec.get().transform().apply(arg);
							} catch (RuntimeException e) {
								throw new IllegalArgumentException(e);
							}
							ret.put(flagSpec.get().destination(), transformArg);
						} else {
							throw new IllegalArgumentException("Mandatory parameter required for --" + arg);
						}
					}
				}
			} else if (arg.startsWith("-")) {
				// Strip away leading '-'
				arg = arg.substring(1);

				while (!arg.isEmpty()) {
					char s = arg.charAt(0);
					arg = arg.substring(1);
					Optional<UnixFlagSpec> flagSpec =
							arguments.stream()
							         .filter((flag) -> (flag.shortArg().isPresent() && flag.shortArg().get() == s))
							         .findFirst();

					if (flagSpec.isEmpty()) {
						throw new IllegalArgumentException("Unrecognized short option -" + s);
					}

					switch (flagSpec.get().type()) {
						case EXIST -> ret.put(flagSpec.get().destination(), true);
						case PARAMETRIZE -> {
							Object transformArg;
							if (!arg.isEmpty()) {
								// Parameter is the remainder of the arg string
								try {
									transformArg = flagSpec.get().transform().apply(arg);
								} catch (RuntimeException e) {
									throw new IllegalArgumentException(e);
								}
								ret.put(flagSpec.get().destination(), transformArg);
								arg = "";
							} else if (itr.hasNext()) {
								String localArg = itr.next();
								try {
									transformArg = flagSpec.get().transform().apply(localArg);
								} catch (RuntimeException e) {
									throw new IllegalArgumentException(e);
								}
								ret.put(flagSpec.get().destination(), transformArg);
							} else {
								throw new IllegalArgumentException("Mandatory parameter required for -" + s);
							}
						}
					}
				}
			}
		}

		return ret;
	}
}
