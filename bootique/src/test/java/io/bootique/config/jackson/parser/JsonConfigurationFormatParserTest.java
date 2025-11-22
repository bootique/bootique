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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class JsonConfigurationFormatParserTest {

    @Test
    public void parse() {
        InputStream in = new ByteArrayInputStream("{\"a\":\"b\",\"b\": \"c\"}".getBytes());
        JacksonService jacksonService = new DefaultJacksonService();

        JsonNode node = new JsonConfigurationFormatParser(jacksonService).parse(in);
        assertNotNull(node);

        assertEquals("b", node.get("a").asText());
        assertEquals("c", node.get("b").asText());
    }

    @Deprecated
    @Test
    public void shouldParse() throws MalformedURLException {
        JacksonService jacksonService = new DefaultJacksonService();
        ConfigurationFormatParser parser = new JsonConfigurationFormatParser(jacksonService);

        assertTrue(parser.shouldParse(new URL("file://tmp/test.json"), ""));
        assertTrue(parser.shouldParse(new URL("file://tmp/test.json"), null));
        assertTrue(parser.shouldParse(new URL("file://tmp/test.json"), "application/x-unknown"));
        assertTrue(parser.shouldParse(new URL("file://tmp/test.json?query=abc"), ""));

        assertTrue(parser.shouldParse(new URL("https://example.com/test.json?query=abc"), ""));
        assertTrue(parser.shouldParse(new URL("https://example.com/test.json?query=abc"), null));
        assertTrue(parser.shouldParse(new URL("https://example.com/test.json?query=abc"), "application/json"));
        assertTrue(parser.shouldParse(new URL("https://example.com/test?query=abc"), "application/json"));
        assertTrue(parser.shouldParse(new URL("https://example.com/test.js?query=abc"), "application/json"));

        assertFalse(parser.shouldParse(new URL("https://example.com/test.js?query=json"), "application/x-unknown"));
        assertFalse(parser.shouldParse(new URL("https://example.com/test.js?query=json"), ""));
        assertFalse(parser.shouldParse(new URL("https://example.com/test.js?query=json"), null));
    }


    @Test
    public void supportsLocation() throws MalformedURLException, URISyntaxException {
        JacksonService jacksonService = new DefaultJacksonService();
        ConfigurationFormatParser parser = new JsonConfigurationFormatParser(jacksonService);

        assertTrue(parser.supportsLocation(new URI("file://tmp/test.json").toURL()));
        assertTrue(parser.supportsLocation(new URI("file://tmp/test.json?query=abc").toURL()));

        assertTrue(parser.supportsLocation(new URI("https://example.com/test.json?query=abc").toURL()));
        assertFalse(parser.supportsLocation(new URI("https://example.com/test").toURL()));
        assertFalse(parser.supportsLocation(new URI("https://example.com/test.js?query=abc").toURL()));
        assertFalse(parser.supportsLocation(new URI("https://example.com/test.js?query=json").toURL()));
    }

    @Test
    public void supportsContentType() {
        JacksonService jacksonService = new DefaultJacksonService();
        ConfigurationFormatParser parser = new JsonConfigurationFormatParser(jacksonService);

        assertFalse(parser.supportsContentType(null));
        assertFalse(parser.supportsContentType(""));
        assertFalse(parser.supportsContentType("application/x-unknown"));
        assertTrue(parser.supportsContentType("application/json"));
    }

}
