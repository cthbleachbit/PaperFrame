package me.cth451.paperframe.util.getopt;

import org.jetbrains.annotations.Contract;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Unescape {
	/*
	 * This operates as two-pass state machine.
	 * In the first pass, all end-of-token backslashes are replaced and appropriate action is tagged
	 */
	enum Action {
		/*
		 * Placeholder action
		 */
		TBD,
		/*
		 * A token marked continue will be appended to the internal staging buffer.
		 */
		CONTINUE,
		/*
		 * Upon encountering a token marked COMMIT:
		 * - the token is appended to the internal staging buffer
		 * - the internal staging buffer is returned as a single token on the output list
		 * - the internal staging buffer is cleared
		 */
		COMMIT,
	}

	static final Function<String, Map.Entry<String, Action>> evaluateArg = s -> {
		/* Find out how many backslash there is at the end of the token */
		int idx = s.length();
		while (idx > 0 && s.charAt(idx - 1) == '\\') {
			idx--;
		}
		int numOfSlash = s.length() - idx;
		if (numOfSlash <= 0) {
			return Map.entry(s, Action.COMMIT);
		} else if (numOfSlash % 2 == 0) {
			/* Need to replace '\\'s into '\' */
			String r = s.replace("\\\\", "\\");
			return Map.entry(r, Action.COMMIT);
		} else {
			/* Replace last '\' with space and '\\'s into '\'s */
			String r = s.replaceAll("\\\\$", " ").replace("\\\\", "\\");
			return Map.entry(r, Action.CONTINUE);
		}
	};

	static class ActionEvaluator implements Consumer<Map.Entry<String, Action>> {
		private final StringBuilder buffer = new StringBuilder();
		private final List<String> normalized;

		public ActionEvaluator(List<String> normalized) {
			this.normalized = normalized;
		}

		@Override
		public void accept(Map.Entry<String, Action> i) {
			String s = i.getKey();
			Action action = i.getValue();
			switch (action) {
				case TBD -> throw new RuntimeException("TBD is not allowed during action evaluation!");
				case CONTINUE -> buffer.append(s);
				case COMMIT -> {
					buffer.append(s);
					normalized.add(buffer.toString());
					buffer.setLength(0);
				}
			}
		}

		public StringBuilder getBuffer() {
			return buffer;
		}
	}

	/**
	 * Normalize backslash escapes for white space. Definitely not conforming to standards but should be good enough.
	 * <p>
	 * '\' at the end of one argument: if there is the next argument, '\' is replaced as a space, and next argument is
	 * appended to current with a space, or if this is the last argument, '\' is replaced with a space.
	 * <p>
	 * '\\' at the end of one argument: '\\' replaced with a single '\'.
	 * <p>
	 * Examples:
	 * <ul>
	 *     <li>'arg1\' -> 'arg1 '</li>
	 *     <li>'arg1\' 'arg2' -> 'arg1 arg2'</li>
	 *     <li>'arg1\\' 'arg2' -> 'arg1\' 'arg2'</li>
	 *     <li>'arg1\' 'arg2\' -> 'arg1 arg2 '</li>
	 * </ul>
	 *
	 * @param argv1p vanilla argv1p
	 * @return normalized
	 */
	@Contract(pure = true)
	public static List<String> normalizeArgv1p(List<String> argv1p) {
		/* Output */
		final List<String> normalized = new LinkedList<>();

		ActionEvaluator evaluateAction = new ActionEvaluator(normalized);
		argv1p.stream().map(evaluateArg).forEach(evaluateAction);

		StringBuilder lastState = evaluateAction.getBuffer();
		/* In case that last token ends with '/' */
		if (!lastState.isEmpty()) {
			normalized.add(lastState.toString());
		}

		return normalized;
	}
}
