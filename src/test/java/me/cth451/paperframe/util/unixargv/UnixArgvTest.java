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
		minemap.add(new UnixFlagSpec("dithering", 'd', UnixFlagSpec.FlagType.EXIST, "dithering"));
		minemap.add(new UnixFlagSpec("input", 'i', UnixFlagSpec.FlagType.PARAMETRIZE, "input"));
		minemap.add(new UnixFlagSpec("no-gz", (char) 0, UnixFlagSpec.FlagType.EXIST, "no-gz"));
		minemap.add(new UnixFlagSpec("output", 'c', UnixFlagSpec.FlagType.PARAMETRIZE, "output"));
		minemap.add(new UnixFlagSpec("export", 'e', UnixFlagSpec.FlagType.PARAMETRIZE, "export"));
		minemap.add(new UnixFlagSpec("game", 'g', UnixFlagSpec.FlagType.PARAMETRIZE, "game"));

		numerical.add(new UnixFlagSpec("string", (char) 0, UnixFlagSpec.FlagType.PARAMETRIZE, "string"));
		numerical.add(new UnixFlagSpec("integer", (char) 0, UnixFlagSpec.FlagType.PARAMETRIZE, "integer", Integer::parseInt));
		numerical.add(new UnixFlagSpec("float", (char) 0, UnixFlagSpec.FlagType.PARAMETRIZE, "float", Float::parseFloat));
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