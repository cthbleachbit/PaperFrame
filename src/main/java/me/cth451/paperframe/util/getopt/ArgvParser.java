package me.cth451.paperframe.util.getopt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A GNU getopt style argument parser (reduced functionality - no positional arguments)
 */
public class ArgvParser {
	private final Collection<UnixFlagSpec> arguments;
	private final HashMap<String, UnixFlagSpec> longLookupTable = new HashMap<>();
	private final HashMap<Character, UnixFlagSpec> shortLookupTable = new HashMap<>();

	/**
	 * Verify whether the arguments make sense:
	 * <ul>
	 *     <li>check for conflicting long options</li>
	 *     <li>check for conflicting short hands</li>
	 *     <li>check for conflicting destination variable names</li>
	 * </ul>
	 * <p>
	 * This method only need to execute once during construction.
	 *
	 * @throws IllegalArgumentException if the argv specification violates the rules above
	 */
	@Contract(mutates = "this")
	private void verify() throws IllegalArgumentException {
		// Populate lookup structure and count distinct long options
		arguments.forEach((arg) -> longLookupTable.put(arg.longArg(), arg));
		if (longLookupTable.size() != arguments.size()) {
			throw new IllegalArgumentException("Duplicated long argument names detected");
		}

		// count flags that has a shorthand defined
		long flagsWithShorthands =
				arguments.stream()
				         .filter((arg) -> arg.shortArg() > 0)
				         .count();
		// Populate lookup structure and count distinct short options
		arguments.stream()
		         .filter((arg) -> arg.shortArg() > 0)
		         .forEach((arg) -> shortLookupTable.put(arg.shortArg(), arg));
		if (flagsWithShorthands != shortLookupTable.size()) {
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
	public ArgvParser(Collection<UnixFlagSpec> arguments) throws IllegalArgumentException {
		this.arguments = arguments;
		verify();
	}

	/**
	 * Parse given list of argv1 and on into key-value pairs.
	 * <p>
	 * If the type of {@link UnixFlagSpec} is EXIST, the resulting destination variable will contain True/False.
	 * <p>
	 * If the type of {@link UnixFlagSpec} is PARAMETRIZE, the destination variable may
	 * <ul>
	 *     <li>either do not exist in the mapping,</li>
	 *     <li>be found in the mapping by the destination name after applying {@link UnixFlagSpec#transform()}.</li>
	 * </ul>
	 * This method does not modify internal states and should be safe to call concurrently.
	 *
	 * @param argv1pWithBackslash ordered list of arguments from argv[1] and on
	 * @return mapping of parsed arguments and parameters
	 * @throws IllegalArgumentException if the input string array contains unrecognized options
	 */
	@Contract(pure = true)
	public HashMap<String, Object> parse(@NotNull List<String> argv1pWithBackslash) throws IllegalArgumentException {
		return parse(argv1pWithBackslash, null);
	}

	/**
	 * Parse given list of argv1 and on into key-value pairs.
	 * <p>
	 * If the type of {@link UnixFlagSpec} is EXIST, the resulting destination variable will contain True/False.
	 * <p>
	 * If the type of {@link UnixFlagSpec} is PARAMETRIZE, the destination variable may
	 * <ul>
	 *     <li>either do not exist in the mapping,</li>
	 *     <li>be found in the mapping by the destination name after applying {@link UnixFlagSpec#transform()}.</li>
	 * </ul>
	 * This method does not modify internal states and should be safe to call concurrently.
	 *
	 * @param argv1pWithBackslash ordered list of arguments from argv[1] and on
	 * @param extraTokens         if set to nonnull, parsing will stop upon encountering an unknown token. The remaining
	 *                            tokens are written here. If this is set to null, then unrecognized parameters will
	 *                            cause IllegalArgumentException.
	 * @return mapping of parsed arguments and parameters
	 * @throws IllegalArgumentException if the input string array contains unrecognized options and extraTokens is
	 *                                  null.
	 */
	public HashMap<String, Object> parse(@NotNull List<String> argv1pWithBackslash, @Nullable List<String> extraTokens)
			throws IllegalArgumentException {
		List<String> argv1p = Unescape.normalizeArgv1p(argv1pWithBackslash);

		// Create defaulted existence flags
		HashMap<String, Object> ret = new HashMap<>();
		arguments.stream()
		         .filter((arg) -> arg.type() == UnixFlagSpec.FlagType.EXIST)
		         .forEach((arg) -> ret.put(arg.destination(), false));

		Iterator<String> itr = argv1p.iterator();

		BiConsumer<Iterator<String>, List<String>> appendRest = (itr1, strings) -> {
			while (itr1.hasNext()) {
				strings.add(itr1.next());
			}
		};

		while (itr.hasNext()) {
			String arg = itr.next();
			if (arg.startsWith("--")) {
				// Long argument - Strip away leading "--"
				arg = arg.substring(2);
				if (!longLookupTable.containsKey(arg)) {
					if (extraTokens == null) {
						throw new IllegalArgumentException("Unrecognized long option --" + arg);
					} else {
						/* Bail out of the loop */
						extraTokens.add(arg);
						appendRest.accept(itr, extraTokens);
						break;
					}
				}

				UnixFlagSpec flagSpec = longLookupTable.get(arg);
				switch (flagSpec.type()) {
					case EXIST -> ret.put(flagSpec.destination(), true);
					case PARAMETRIZE -> {
						if (itr.hasNext()) {
							arg = itr.next();
							Object transformArg;
							try {
								transformArg = flagSpec.transform().apply(arg);
							} catch (RuntimeException e) {
								throw new IllegalArgumentException(e);
							}
							ret.put(flagSpec.destination(), transformArg);
						} else {
							throw new IllegalArgumentException("Mandatory parameter required for --" + arg);
						}
					}
				}
			} else if (arg.startsWith("-")) {
				// Strip away leading '-'
				arg = arg.substring(1);

				while (!arg.isEmpty()) {
					// Consume one character
					char s = arg.charAt(0);
					arg = arg.substring(1);

					if (!shortLookupTable.containsKey(s)) {
						if (extraTokens == null) {
							throw new IllegalArgumentException("Unrecognized short option -" + s);
						} else {
							/* Bail out of the loop */
							extraTokens.add(arg);
							appendRest.accept(itr, extraTokens);
							break;
						}
					}

					UnixFlagSpec flagSpec = shortLookupTable.get(s);
					switch (flagSpec.type()) {
						case EXIST -> ret.put(flagSpec.destination(), true);
						case PARAMETRIZE -> {
							Object transformArg;
							String localArg;

							// Find the rest of the parameter
							if (!arg.isEmpty()) {
								// Parameter is the remainder of the arg string
								localArg = arg;
								arg = "";
							} else if (itr.hasNext()) {
								// Parameter is the next token
								localArg = itr.next();
							} else {
								throw new IllegalArgumentException("Mandatory parameter required for -" + s);
							}

							try {
								transformArg = flagSpec.transform().apply(localArg);
							} catch (RuntimeException e) {
								throw new IllegalArgumentException(e);
							}
							ret.put(flagSpec.destination(), transformArg);
						}
					}
				}
			} else {
				if (extraTokens == null) {
					throw new IllegalArgumentException("Unexpected parameter" + arg);
				} else {
					/* Bail out of the loop */
					extraTokens.add(arg);
					appendRest.accept(itr, extraTokens);
					break;
				}
			}
		}

		return ret;
	}
}
