package com.nhl.bootique.config.jackson;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import io.bootique.config.jackson.MultiFormatJsonNodeParser;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.config.jackson.MultiFormatJsonNodeParser.ParserType;
import io.bootique.log.BootLogger;

public class MultiFormatJsonNodeParserTest {

	@SuppressWarnings("unchecked")
	private Map<ParserType, Function<InputStream, JsonNode>> createParsersMap(ParserType... types) {

		Map<ParserType, Function<InputStream, JsonNode>> map = new EnumMap<>(ParserType.class);
		asList(types).forEach(t -> map.put(t, mock(Function.class)));

		return map;
	}

	@Test
	public void testParser() {
		Map<ParserType, Function<InputStream, JsonNode>> parsers = createParsersMap(ParserType.JSON, ParserType.YAML);

		MultiFormatJsonNodeParser parser = new MultiFormatJsonNodeParser(parsers, mock(BootLogger.class));
		assertSame(parsers.get(ParserType.YAML), parser.parser(ParserType.YAML));
		assertSame(parsers.get(ParserType.JSON), parser.parser(ParserType.JSON));
	}

	@Test(expected = IllegalStateException.class)
	public void testParser_MissingYaml() {
		Map<ParserType, Function<InputStream, JsonNode>> parsers = createParsersMap(ParserType.JSON);
		new MultiFormatJsonNodeParser(parsers, mock(BootLogger.class)).parser(ParserType.YAML);
	}

	@Test
	public void testParserTypeFromExtension_Unknown() throws MalformedURLException {

		MultiFormatJsonNodeParser parser = new MultiFormatJsonNodeParser(Collections.emptyMap(),
				mock(BootLogger.class));

		assertNull(parser.parserTypeFromExtension(new URL("http://example.org/test")));
		assertNull(parser.parserTypeFromExtension(new URL("http://example.org/")));
		assertNull(parser.parserTypeFromExtension(new URL("http://example.org/test.txt")));
	}

	@Test
	public void testParserTypeFromExtension() throws MalformedURLException {

		MultiFormatJsonNodeParser parser = new MultiFormatJsonNodeParser(Collections.emptyMap(),
				mock(BootLogger.class));

		assertEquals(ParserType.YAML, parser.parserTypeFromExtension(new URL("http://example.org/test.yml")));
		assertEquals(ParserType.YAML, parser.parserTypeFromExtension(new URL("http://example.org/test.yaml")));
		assertEquals(ParserType.JSON, parser.parserTypeFromExtension(new URL("http://example.org/test.json")));
	}

	@Test
	public void testParserTypeFromExtension_IgnoreQuery() throws MalformedURLException {

		MultiFormatJsonNodeParser parser = new MultiFormatJsonNodeParser(Collections.emptyMap(),
				mock(BootLogger.class));
		assertEquals(ParserType.JSON, parser.parserTypeFromExtension(new URL("http://example.org/test.json?a=b")));
	}

	@Test
	public void testParserTypeFromExtension_FileUrl() throws MalformedURLException {

		MultiFormatJsonNodeParser parser = new MultiFormatJsonNodeParser(Collections.emptyMap(),
				mock(BootLogger.class));
		assertEquals(ParserType.YAML, parser.parserTypeFromExtension(new URL("file://example.org/test.yml")));
	}
}
