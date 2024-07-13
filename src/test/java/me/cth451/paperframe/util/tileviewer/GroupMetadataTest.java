package me.cth451.paperframe.util.tileviewer;

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class GroupMetadataTest {
	@Test
	public void testBasic() {
		GroupMetadata metadata = GroupMetadata.fromJson("{\"name\": \"Non Linear Central Transit System Map\", \"geometry\": [[113, 114], [115, 116]], \"desc\": [\"2nd iteration after the network expanded beyond the first geographically accurate map\"], \"links\": {}, \"usage_hints\": {\"has_transparency\": false}}");

		System.out.println(metadata);
	}
}
