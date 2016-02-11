package com.nhl.bootique.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.bootique.config.YamlConfigurationFactory;

public class YamlConfigurationFactory_StaticsTest {

	@Test
	public void testReadYaml() {

		InputStream in = new ByteArrayInputStream("a: b\nb: c".getBytes());
		ObjectMapper mapper = new ObjectMapper();

		JsonNode node = YamlConfigurationFactory.readYaml(in, mapper);
		assertNotNull(node);

		assertEquals("b", node.get("a").asText());
		assertEquals("c", node.get("b").asText());
	}
}
