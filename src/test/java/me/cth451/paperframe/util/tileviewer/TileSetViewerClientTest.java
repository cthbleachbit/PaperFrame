package me.cth451.paperframe.util.tileviewer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TileSetViewerClientTest {
	@ParameterizedTest
	@CsvSource(value = {
			"/CoverArt/迷跡波/:CoverArt/迷跡波",
			"NonLinearMap-Hologram/:NonLinearMap-Hologram",
			"Arrival Boards/Purple/:Arrival Boards/Purple"
	}, delimiter = ':')
	public void testPathNormalization(String input, String expectedOutput) {
		String actual = TileSetViewerClient.normalizePath(input);
		assertEquals(expectedOutput, actual);
	}
}
