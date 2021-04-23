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
import io.bootique.BootiqueException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

public class MultiFormatJsonNodeParser implements JsonConfigurationParser {

    private final Set<ConfigurationFormatParser> parsers;

    public MultiFormatJsonNodeParser(Set<ConfigurationFormatParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public JsonNode parse(URL url) {
        URLConnection connection = openConnection(url);

        ConfigurationFormatParser parser = parserForUrl(url, connection.getContentType());
        if(parser == null) {
            throw new BootiqueException(1, "Can't find suitable parser for the configuration resource " + url);
        }

        try (InputStream in = connection.getInputStream()) {
            return parser.parse(in);
        } catch (IOException e) {
            throw new BootiqueException(1, "Config resource is not found or is inaccessible: " + url, e);
        }
    }

    ConfigurationFormatParser parserForUrl(URL url, String contentType) {
        for(ConfigurationFormatParser parser: parsers) {
            if(parser.shouldParse(url, contentType)) {
                return parser;
            }
        }
        return null;
    }

    URLConnection openConnection(URL url) {
        URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            // The message is dumb. But we don't really expect an exception (as no connection is established here),
            // and don't have a test case to reproduce.
            // TODO: If we ever see this condition occur, perhaps we can create a better message?
            throw new BootiqueException(1, "Can't create connection to config resource: " + url, e);
        }
        return connection;
    }
}
