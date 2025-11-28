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

import io.bootique.BootiqueException;
import io.bootique.jackson.DefaultJacksonService;
import io.bootique.jackson.JacksonService;
import io.bootique.resource.ResourceFactory;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiFormatJsonNodeParserTest {

    JacksonService jacksonService = new DefaultJacksonService();
    ConfigurationFormatParser jsonParser = new JsonConfigurationFormatParser(jacksonService);
    ConfigurationFormatParser yamlParser = new YamlConfigurationFormatParser(jacksonService);

    private Set<ConfigurationFormatParser> createParsersSet() {
        return new HashSet<>(Arrays.asList(jsonParser, yamlParser));
    }

    @Test
    public void parser() throws MalformedURLException {
        MultiFormatJsonNodeParser parser = new MultiFormatJsonNodeParser(createParsersSet());

        assertSame(jsonParser, parser.parser(null, URI.create("http://example.org/test.json").toURL()));
        assertSame(jsonParser, parser.parser("", URI.create("http://example.org/test.json").toURL()));

        assertSame(jsonParser, parser.parser("application/json", URI.create("http://example.org/test").toURL()));
        assertSame(jsonParser, parser.parser("", URI.create("http://example.org/test.json?test=abc").toURL()));

        assertSame(jsonParser, parser.parser("application/json", new ResourceFactory("stdin:json").getUrl()));

        assertSame(yamlParser, parser.parser(null, URI.create("http://example.org/test.yml").toURL()));
        assertSame(yamlParser, parser.parser("", URI.create("http://example.org/test.yml").toURL()));
        assertSame(yamlParser, parser.parser("", URI.create("http://example.org/test.yaml").toURL()));
        assertSame(yamlParser, parser.parser("", URI.create("http://example.org/test.yaml?test=abc").toURL()));
        assertSame(yamlParser, parser.parser("application/x-yaml", URI.create("http://example.org/test").toURL()));

        assertSame(yamlParser, parser.parser("application/x-yaml", new ResourceFactory("stdin:yaml").getUrl()));

        assertThrows(BootiqueException.class, () -> parser.parser("", URI.create("http://example.org/test").toURL()));
    }
}
