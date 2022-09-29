package me.cth451.paperframe.util.unixargv;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnixArgvTest {
	static HashSet<UnixFlagSpec> minemap = new HashSet<>();;
	static HashSet<UnixFlagSpec> numerical = new HashSet<>();

	@BeforeAll
	static void setUp() {
		minemap.add(new UnixFlagSpec("dithering", Optional.of('d'), FlagType.EXIST, "dithering"));
		minemap.add(new UnixFlagSpec("input", Optional.of('i'), FlagType.PARAMETRIZE, "input"));
		minemap.add(new UnixFlagSpec("no-gz", Optional.empty(), FlagType.EXIST, "no-gz"));
		minemap.add(new UnixFlagSpec("output", Optional.of('c'), FlagType.PARAMETRIZE, "output"));
		minemap.add(new UnixFlagSpec("export", Optional.of('e'), FlagType.PARAMETRIZE, "export"));
		minemap.add(new UnixFlagSpec("game", Optional.of('g'), FlagType.PARAMETRIZE, "game"));

		numerical.add(new UnixFlagSpec("string", Optional.empty(), FlagType.PARAMETRIZE, "string"));
		numerical.add(new UnixFlagSpec("integer", Optional.empty(), FlagType.PARAMETRIZE, "integer", Integer::parseInt));
		numerical.add(new UnixFlagSpec("float", Optional.empty(), FlagType.PARAMETRIZE, "float", Float::parseFloat));
	}

	@Test
	void parseValidTest1() {
		UnixArgv parser = new UnixArgv(minemap);
		String[] input = {"-d", "-i", "blah", "--game", "1.17"};
		HashMap<String, Object> parsed = parser.parse(input);
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
		UnixArgv parser = new UnixArgv(minemap);
		String[] input = {"-d", "-iblah", "-g1.17"};
		HashMap<String, Object> parsed = parser.parse(input);
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
		UnixArgv parser = new UnixArgv(minemap);
		String[] input = {"-d", "-iblah", "--random-shit"};
		assertThrowsExactly(IllegalArgumentException.class, () -> parser.parse(input), "Unrecognized long option --random-shit");
	}

	@Test
	void parseUnknownArgumentsTest2() {
		UnixArgv parser = new UnixArgv(minemap);
		String[] input = {"-d", "-x", "-iblah", "-r"};
		assertThrowsExactly(IllegalArgumentException.class, () -> parser.parse(input), "Unrecognized short option -x");
	}

	@Test
	void parseValidNumerical() {
		UnixArgv parser = new UnixArgv(numerical);
		String[] input = {"--string", "str", "--float", "0.8347", "--integer", "42"};
		HashMap<String, Object> parsed = parser.parse(input);
		assertEquals(parsed.size(), 3);
		assertEquals(parsed.get("string"), "str");
		assertEquals(parsed.get("float"), 0.8347f);
		assertEquals(parsed.get("integer"), 42);
	}

	@Test
	void parseMalformNumerical() {
		UnixArgv parser = new UnixArgv(numerical);
		String[] input = {"--string", "str", "--float", "0.83ddd47", "--integer", "42"};
		assertThrows(IllegalArgumentException.class, () -> parser.parse(input));
	}
}