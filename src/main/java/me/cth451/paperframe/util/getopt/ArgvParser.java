package me.cth451.paperframe.util.getopt;

import com.google.common.collect.Iterables;
import me.cth451.paperframe.command.base.IAsyncTabCompleteExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A GNU getopt style argument parser (reduced functionality - no positional arguments)
 */
public class ArgvParser {
	private final Collection<UnixFlagSpec> arguments = new ArrayList<>();
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
		long flagsWithLongArgs =
				arguments.stream()
				         .filter((arg) -> arg.longArg() != null && !arg.longArg().isEmpty())
				         .count();
		arguments.stream()
		         .filter((arg) -> arg.longArg() != null && !arg.longArg().isEmpty())
		         .forEach((arg) -> longLookupTable.put(arg.longArg(), arg));
		if (longLookupTable.size() != flagsWithLongArgs) {
			throw new IllegalArgumentException("Duplicated long argument names detected");
		}

		// count flags that has a shorthand defined
		long flagsWithShorthands =
				arguments.stream()
				         .filter((arg) -> arg.shortArg() != null && arg.shortArg() > 0)
				         .count();
		// Populate lookup structure and count distinct short options
		arguments.stream()
		         .filter((arg) -> arg.shortArg() != null && arg.shortArg() > 0)
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
		this(arguments, true);
	}

	/**
	 * Construct an argument parser based on the flag list provided
	 *
	 * @param arguments flag specification
	 * @throws IllegalArgumentException when flags are inconsistent
	 */
	public ArgvParser(Collection<UnixFlagSpec> arguments, boolean includeHelp) throws IllegalArgumentException {
		this.arguments.addAll(arguments);
		if (includeHelp) {
			this.arguments.add(new UnixFlagSpec("help", null, UnixFlagSpec.FlagType.EXIST, "help"));
		}
		verify();
	}


	@Contract(pure = true)
	public HashMap<String, Object> parse(@NotNull List<String> argv1pWithBackslash) throws IllegalArgumentException, PrintHelpException, ParameterRequiredException {
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
	 * If ArgvParser was constructed with includeHelp set to true, parsing will end upon encountering "-h" and a
	 * {@link PrintHelpException} will be raised.
	 * <p>
	 * This method does not modify internal states and should be safe to call concurrently.
	 *
	 * @param argv1pWithBackslash ordered list of arguments from argv[1] and on
	 * @param extraTokens         If provided, parsing will stop upon encountering an unknown token. All remaining
	 *                            unparsed tokens are backslash-unescaped and written here.
	 *                            If this is set to null, any unrecognized flags will cause IllegalArgumentException.
	 * @return mapping of parsed arguments and parameters
	 * @throws PrintHelpException         if includeHelp was specified in constructor and "-h" was passed by user.
	 * @throws IllegalArgumentException   if the input string array contains unrecognized options and extraTokens is
	 *                                    null.
	 * @throws ParameterRequiredException if a PARAMETRIZE option requires a mandatory parameter that isn't passed
	 */
	public HashMap<String, Object> parse(@NotNull List<String> argv1pWithBackslash, @Nullable List<String> extraTokens)
			throws IllegalArgumentException, PrintHelpException, ParameterRequiredException {
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
							throw new ParameterRequiredException(flagSpec, true);
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
								throw new ParameterRequiredException(flagSpec, false);
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

		if ((boolean) ret.get("help")) {
			throw new PrintHelpException();
		}

		return ret;
	}

	/**
	 * Provide tag completion results for command
	 *
	 * @param argv1pWithBackslash ordered list of arguments from argv[1] and on with no trailing empty strings
	 * @param lastArgComplete     whether last argument is complete
	 * @return possible completions
	 * @throws ParameterRequiredException if the completion needs an argument for a PARAMETRIZE option
	 */
	public List<IAsyncTabCompleteExecutor.CompletionResult> tabComplete(@NotNull List<String> argv1pWithBackslash,
	                                                                    boolean lastArgComplete) throws ParameterRequiredException {
		List<String> extraTokens = new LinkedList<>();

		if (!argv1pWithBackslash.isEmpty()) {
			try {
				/* If last arg is incomplete consider it missing - let parser figure out what to do */
				List<String> parsableTokens =
						argv1pWithBackslash.subList(0, argv1pWithBackslash.size() - (lastArgComplete ? 0 : 1));

				/*
				 * ParameterRequiredException to be handled by caller.
				 * Should never see IllegalArguments as unknown tokens are stored to extraTokens.
				 */
				this.parse(parsableTokens, extraTokens);
			} catch (PrintHelpException e) {
				return List.of();
			}
		}

		String nextToken;
		if (!lastArgComplete) {
			nextToken = Iterables.getLast(argv1pWithBackslash, "");
		} else if (!extraTokens.isEmpty()) {
			nextToken = extraTokens.get(0);
		} else {
			nextToken = "";
		}

		Set<IAsyncTabCompleteExecutor.CompletionResult> completions = new HashSet<>();
		this.longLookupTable.values().stream()
		                    .filter((f) -> ("--" + f.longArg()).startsWith(nextToken))
		                    .map((f) -> new IAsyncTabCompleteExecutor.CompletionResult("--" + f.longArg(), null))
		                    .forEach(completions::add);
		this.shortLookupTable.values().stream()
		                     .filter((f) -> ("-" + f.shortArg()).startsWith(nextToken))
		                     .map((f) -> new IAsyncTabCompleteExecutor.CompletionResult("-" + f.shortArg(), null))
		                     .forEach(completions::add);

		return completions.stream().toList();
	}
}
