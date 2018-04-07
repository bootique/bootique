package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PathSegmentTest {


	@Test
	public void testLastPathComponent_Root() {
		JsonNode node = YamlReader.read("a: b\nc: d");
		Optional<PathSegment> last = new PathSegment(node, "", '.').lastPathComponent();

		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("b", last.get().getNode().get("a").asText());
		assertEquals("d", last.get().getNode().get("c").asText());
	}

	@Test
	public void testLastPathComponent() {
		JsonNode node = YamlReader.read("a: b\nc: d");
		Optional<PathSegment> last = new PathSegment(node, "a", '.').lastPathComponent();

		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("b", last.get().getNode().asText());
	}

	@Test
	public void testLastPathComponent_Nested() {
		JsonNode node = YamlReader.read("a: b\nc:\n  d: e");
		Optional<PathSegment> last = new PathSegment(node, "c.d", '.').lastPathComponent();

		assertNotNull(last);
		assertEquals("e", last.get().getNode().asText());
	}
}
