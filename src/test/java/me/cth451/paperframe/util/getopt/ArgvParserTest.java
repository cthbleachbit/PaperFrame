package me.cth451.paperframe.util.getopt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ArgvParserTest {
	static HashSet<UnixFlagSpec> minemap = new HashSet<>();
	;
	static HashSet<UnixFlagSpec> numerical = new HashSet<>();

	@BeforeAll
	static void setUp() {
		minemap.add(new UnixFlagSpec("dithering", 'd', UnixFlagSpec.FlagType.EXIST, "dithering"));
		minemap.add(new UnixFlagSpec("input", 'i', UnixFlagSpec.FlagType.PARAMETRIZE, "input"));
		minemap.add(new UnixFlagSpec("no-gz", (char) 0, UnixFlagSpec.FlagType.EXIST, "no-gz"));
		minemap.add(new UnixFlagSpec("output", 'c', UnixFlagSpec.FlagType.PARAMETRIZE, "output"));
		minemap.add(new UnixFlagSpec("export", 'e', UnixFlagSpec.FlagType.PARAMETRIZE, "export"));
		minemap.add(new UnixFlagSpec("game", 'g', UnixFlagSpec.FlagType.PARAMETRIZE, "game"));

		numerical.add(new UnixFlagSpec("string", (char) 0, UnixFlagSpec.FlagType.PARAMETRIZE, "string"));
		numerical.add(
				new UnixFlagSpec("integer", (char) 0, UnixFlagSpec.FlagType.PARAMETRIZE, "integer", Integer::parseInt));
		numerical.add(
				new UnixFlagSpec("float", (char) 0, UnixFlagSpec.FlagType.PARAMETRIZE, "float", Float::parseFloat));
	}

	@Test
	void parseValidTest1() {
		ArgvParser parser = new ArgvParser(minemap);
		String[] input = {"-d", "-i", "blah", "--game", "1.17"};
		HashMap<String, Object> parsed = parser.parse(List.of(input));
		// should get 2 EXIST type flag and 2 PARAMETRIZE type flag
		assertEquals(parsed.size(), 4);
		assertEquals(parsed.get("dithering"), true);
		assertEquals(parsed.get("input"), "blah");
		assertEquals(parsed.get("no-gz"), false);
		assertEquals(parsed.get("game"), "1.17");
		assertFalse(parsed.containsKey("export"));
		assertFalse(parsed.containsKey("output"));
	}

	@Test
	void parseValidTest2() {
		ArgvParser parser = new ArgvParser(minemap);
		String[] input = {"-d", "-iblah", "-g1.17"};
		HashMap<String, Object> parsed = parser.parse(List.of(input));
		// should get 2 EXIST type flag and 2 PARAMETRIZE type flag
		assertEquals(parsed.size(), 4);
		assertEquals(parsed.get("dithering"), true);
		assertEquals(parsed.get("input"), "blah");
		assertEquals(parsed.get("no-gz"), false);
		assertEquals(parsed.get("game"), "1.17");
		assertFalse(parsed.containsKey("export"));
		assertFalse(parsed.containsKey("output"));
	}

	@Test
	void parseUnknownArgumentsTest1() {
		ArgvParser parser = new ArgvParser(minemap);
		String[] input = {"-d", "-iblah", "--random-shit"};
		assertThrowsExactly(IllegalArgumentException.class, () -> parser.parse(List.of(input)),
		                    "Unrecognized long option --random-shit");
	}

	@Test
	void parseUnknownArgumentsTest2() {
		ArgvParser parser = new ArgvParser(minemap);
		String[] input = {"-d", "-x", "-iblah", "-r"};
		assertThrowsExactly(IllegalArgumentException.class, () -> parser.parse(List.of(input)),
		                    "Unrecognized short option -x");
	}

	@Test
	void parseValidNumericalTest() {
		ArgvParser parser = new ArgvParser(numerical);
		String[] input = {"--string", "str", "--float", "0.8347", "--integer", "42"};
		long startTime = System.nanoTime();
		HashMap<String, Object> parsed = parser.parse(List.of(input));
		long endTime = System.nanoTime();
		System.out.println("parseValidNumerical took " + (endTime - startTime) + "ns");
		assertEquals(parsed.size(), 3);
		assertEquals(parsed.get("string"), "str");
		assertEquals(parsed.get("float"), 0.8347f);
		assertEquals(parsed.get("integer"), 42);
	}

	@Test
	void parseMalformedNumericalTest() {
		ArgvParser parser = new ArgvParser(numerical);
		String[] input = {"--string", "str", "--float", "0.83ddd47", "--integer", "42"};
		assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of(input)));
	}

	@Test
	void concurrentNumericalTest1() {
		ArgvParser parser = new ArgvParser(numerical);
		List<String[]> input = new ArrayList<>();
		input.add(new String[]{"--string", "str", "--float", "0.5772156", "--integer", "69"});
		long startTime = System.nanoTime();
		Map<Object, List<HashMap<String, Object>>> results =
				input.parallelStream()
				     .map((argv) -> parser.parse(List.of(argv)))
				     .collect(Collectors.groupingBy((parsed) -> parsed.get("string")));
		long endTime = System.nanoTime();
		System.out.println("concurrentNumericalTest1 took " + (endTime - startTime) + "ns");
		assertEquals(results.size(), 1);
		List<HashMap<String, Object>> strList = results.get("str");
		assertEquals(strList.size(), 1);
		assertEquals(strList.get(0).get("string"), "str");
		assertEquals(strList.get(0).get("float"), 0.5772156f);
		assertEquals(strList.get(0).get("integer"), 69);
	}

	@Test
	void concurrentNumericalTest2() {
		ArgvParser parser = new ArgvParser(numerical);
		List<String[]> input = new ArrayList<>();
		input.add(new String[]{"--string", "str", "--float", "0.5772156", "--integer", "69"});
		input.add(new String[]{"--string", "www", "--float", "3.1415926", "--integer", "42"});
		input.add(new String[]{"--string", "crap"});
		input.add(new String[]{"--string", "uh", "--float", "4839.4"});
		long startTime = System.nanoTime();
		Map<Object, List<HashMap<String, Object>>> results =
				input.parallelStream()
				     .map((argv) -> parser.parse(List.of(argv)))
				     .collect(Collectors.groupingBy((parsed) -> parsed.get("string")));
		long endTime = System.nanoTime();
		System.out.println("concurrentNumericalTest2 took " + (endTime - startTime) + "ns");
		assertEquals(4, results.size());
		List<HashMap<String, Object>> wwwList = results.get("www");
		assertEquals(1, wwwList.size());
		assertEquals("www", wwwList.get(0).get("string"));
		assertEquals(3.1415926f, wwwList.get(0).get("float"));
		assertEquals(42, wwwList.get(0).get("integer"));
		List<HashMap<String, Object>> strList = results.get("str");
		assertEquals(1, strList.size());
		assertEquals("str", strList.get(0).get("string"));
		assertEquals(0.5772156f, strList.get(0).get("float"));
		assertEquals(69, strList.get(0).get("integer"));
	}

	@Test
	void parseEscapeTest1() {
		ArgvParser parser = new ArgvParser(minemap);
		String[] input = {"-d", "-i", "blah\\", "blah blah", "-g1.17"};
		HashMap<String, Object> parsed = parser.parse(List.of(input));
		// should get 2 EXIST type flag and 2 PARAMETRIZE type flag
		assertEquals(parsed.size(), 4);
		assertEquals(parsed.get("dithering"), true);
		assertEquals("blah blah blah", parsed.get("input"));
		assertEquals(parsed.get("no-gz"), false);
		assertEquals(parsed.get("game"), "1.17");
		assertFalse(parsed.containsKey("export"));
		assertFalse(parsed.containsKey("output"));
	}

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
	void strayArgumentsTest1() {
		ArgvParser parser = new ArgvParser(minemap);
		String[] input = {"-d", "-i", "blah\\", "blah blah", "-g1.17", "random shit1", "blahblah"};
		List<String> strayArgs = new LinkedList<>();
		HashMap<String, Object> parsed = parser.parse(List.of(input), strayArgs);
		assertEquals(4, parsed.size());
		assertEquals(2, strayArgs.size());
		assertEquals("random shit1", strayArgs.get(0));
	}
}