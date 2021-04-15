/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.config.jackson.parser;

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.jackson.DefaultJacksonService;
import io.bootique.jackson.JacksonService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YamlConfigurationFormatParserTest {

	@Test
	public void testParse() {

		InputStream in = new ByteArrayInputStream("a: b\nb: c".getBytes());
		JacksonService jacksonService = new DefaultJacksonService();

		JsonNode node = new YamlConfigurationFormatParser(jacksonService).parse(in);
		assertNotNull(node);

		assertEquals("b", node.get("a").asText());
		assertEquals("c", node.get("b").asText());
	}

	@Test
	public void testShouldParse() throws MalformedURLException {
		JacksonService jacksonService = new DefaultJacksonService();
		ConfigurationFormatParser parser = new YamlConfigurationFormatParser(jacksonService);

		assertTrue(parser.shouldParse(new URL("file://tmp/test.yml"), ""));
		assertTrue(parser.shouldParse(new URL("file://tmp/test.yml"), null));
		assertTrue(parser.shouldParse(new URL("file://tmp/test.yml"), "application/x-unknown"));
		assertTrue(parser.shouldParse(new URL("file://tmp/test.yml?query=abc"), ""));

		assertTrue(parser.shouldParse(new URL("https://example.com/test.yaml?query=abc"), ""));
		assertTrue(parser.shouldParse(new URL("https://example.com/test.yaml?query=abc"), null));
		assertTrue(parser.shouldParse(new URL("https://example.com/test.yaml?query=abc"), "application/x-yaml"));
		assertTrue(parser.shouldParse(new URL("https://example.com/test?query=abc"), "application/x-yaml"));
		assertTrue(parser.shouldParse(new URL("https://example.com/test.js?query=abc"), "application/x-yaml"));

		assertFalse(parser.shouldParse(new URL("https://example.com/test.js?query=yaml"), "application/x-unknown"));
		assertFalse(parser.shouldParse(new URL("https://example.com/test.js?query=yaml"), ""));
		assertFalse(parser.shouldParse(new URL("https://example.com/test.js?query=yaml"), null));
	}

}
