package me.cth451.paperframe.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdRangeTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"1:5",
			"1+4",
	})
	void parseOneThruFive(String input) {
		LinkedList<Integer> parsed = IdRange.parseIdRange(input);
		List<Integer> expected = List.of(1, 2, 3, 4, 5);
		assertEquals(expected, parsed);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"5:1",
			"1+-4",
	})
	void parseFiveThruOne(String input) {
		LinkedList<Integer> parsed = IdRange.parseIdRange(input);
		List<Integer> expected = List.of(5, 4, 3, 2, 1);
		assertEquals(expected, parsed);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"5",
			"5:5",
			"5+0",
			"5+-0",
	})
	void parseFive(String input) {
		LinkedList<Integer> parsed = IdRange.parseIdRange(input);
		List<Integer> expected = List.of(5);
		assertEquals(expected, parsed);
	}

}
