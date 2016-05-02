package com.nhl.bootique.config.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

public class PathSegmentTest {

	private static JsonNode readYaml(String yaml) {

		ByteArrayInputStream in = new ByteArrayInputStream(yaml.getBytes());

		try {
			YAMLParser parser = new YAMLFactory().createParser(in);
			return new ObjectMapper().readTree(parser);
		} catch (IOException e) {
			throw new RuntimeException("Error reading config file", e);
		}
	}

	@Test
	public void testLastPathComponent_Root() {
		JsonNode node = readYaml("a: b\nc: d");
		Optional<PathSegment> last = new PathSegment(node, "", '.').lastPathComponent();

		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("b", last.get().getNode().get("a").asText());
		assertEquals("d", last.get().getNode().get("c").asText());
	}

	@Test
	public void testLastPathComponent() {
		JsonNode node = readYaml("a: b\nc: d");
		Optional<PathSegment> last = new PathSegment(node, "a", '.').lastPathComponent();

		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("b", last.get().getNode().asText());
	}

	@Test
	public void testLastPathComponent_Nested() {
		JsonNode node = readYaml("a: b\nc:\n  d: e");
		Optional<PathSegment> last = new PathSegment(node, "c.d", '.').lastPathComponent();

		assertNotNull(last);
		assertEquals("e", last.get().getNode().asText());
	}
}
