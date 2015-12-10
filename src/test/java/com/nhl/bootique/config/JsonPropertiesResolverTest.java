package com.nhl.bootique.config;

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
import com.nhl.bootique.config.JsonPropertiesResolver;
import com.nhl.bootique.config.JsonPropertiesResolver.PathTuple;

public class JsonPropertiesResolverTest {

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
		Optional<PathTuple> last = JsonPropertiesResolver.lastPathComponent(node, "");
		
		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("b", last.get().node.get("a").asText());
		assertEquals("d", last.get().node.get("c").asText());
	}

	@Test
	public void testLastPathComponent() {
		JsonNode node = readYaml("a: b\nc: d");
		Optional<PathTuple> last = JsonPropertiesResolver.lastPathComponent(node, "a");
		
		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("b", last.get().node.asText());
	}
	
	@Test
	public void testLastPathComponent_Nested() {
		JsonNode node = readYaml("a: b\nc:\n  d: e");
		Optional<PathTuple> last = JsonPropertiesResolver.lastPathComponent(node, "c.d");
		
		assertNotNull(last);
		assertNotNull(last.get());
		assertEquals("e", last.get().node.asText());
	}
}
