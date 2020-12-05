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
import io.bootique.log.BootLogger;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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

    @Test
    public void testParser_MissingYaml() {
        Map<ParserType, Function<InputStream, JsonNode>> parsers = createParsersMap(ParserType.JSON);
        assertThrows(IllegalStateException.class, () -> new MultiFormatJsonNodeParser(parsers, mock(BootLogger.class)).parser(ParserType.YAML));
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
