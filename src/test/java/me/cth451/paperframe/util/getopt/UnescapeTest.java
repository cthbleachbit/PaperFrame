package me.cth451.paperframe.util.getopt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnescapeTest {
	@Test
	void unescapeTest1() {
		String[] input = {"arg1\\"};
		List<String> normalized = Unescape.normalizeArgv1p(List.of(input));
		assertEquals(1, normalized.size());
		assertEquals("arg1 ", normalized.get(0));
	}

	@Test
	void unescapeTest2() {
		String[] input = {"arg1\\\\"};
		List<String> normalized = Unescape.normalizeArgv1p(List.of(input));
		assertEquals(1, normalized.size());
		assertEquals("arg1\\", normalized.get(0));
	}

	@Test
	void unescapeTest3() {
		String[] input = {"arg1\\\\\\"};
		List<String> normalized = Unescape.normalizeArgv1p(List.of(input));
		assertEquals(1, normalized.size());
		assertEquals("arg1\\ ", normalized.get(0));
	}

	@Test
	void unescapeTest4() {
		String[] input = {"arg1\\", "\\", "\\"};
		List<String> normalized = Unescape.normalizeArgv1p(List.of(input));
		assertEquals(1, normalized.size());
		assertEquals("arg1   ", normalized.get(0));
	}

	@Test
	void unescapeTest5() {
		String[] input = {"arg1\\", "\\", "\\"};
		List<String> normalized = Unescape.normalizeArgv1p(List.of(input));
		assertEquals(1, normalized.size());
		assertEquals("arg1   ", normalized.get(0));
	}

	@Test
	void tokenizeTestCompleteArg1() {
		String input = "/f2d -h ";
		List<String> normalized = Unescape.tokenize(input);
		assertEquals(List.of("/f2d", "-h", ""), normalized);
	}

	@Test
	void tokenizeTestCompleteArg2() {
		String input = "/f2d -h\\ p ";
		List<String> normalized = Unescape.tokenize(input);
		assertEquals(List.of("/f2d", "-h p", ""), normalized);
	}

	@Test
	void tokenizeTestPartialArg1() {
		String input = "/f2d -h";
		List<String> normalized = Unescape.tokenize(input);
		assertEquals(List.of("/f2d", "-h"), normalized);
	}

	@Test
	void tokenizeTestPartialArg2() {
		String input = "/f2d -h\\ ";
		List<String> normalized = Unescape.tokenize(input);
		assertEquals(List.of("/f2d", "-h "), normalized);
	}

}
