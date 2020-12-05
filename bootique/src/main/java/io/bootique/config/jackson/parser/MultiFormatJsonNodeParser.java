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
import io.bootique.log.BootLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.function.Function;

public class MultiFormatJsonNodeParser implements JsonConfigurationParser {

    private final Map<ParserType, Function<InputStream, JsonNode>> parsers;
    private final BootLogger bootLogger;

    public MultiFormatJsonNodeParser(Map<ParserType, Function<InputStream, JsonNode>> parsers, BootLogger bootLogger) {
        this.parsers = parsers;
        this.bootLogger = bootLogger;
    }

    @Override
    public JsonNode parse(URL url) {

        URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            // The message is dumb. But we don't really expect an exception (as no connection is established here),
            // and don't have a test case to reproduce.
            // TODO: If we ever see this condition occur, perhaps we can create a better message?
            throw new BootiqueException(1, "Can't create connection to config resource: " + url, e);
        }

        ParserType type = parserTypeFromHeaders(connection);

        if (type == null) {
            type = parserTypeFromExtension(url);
        }

        if (type == null) {
            type = ParserType.YAML;
        }

        Function<InputStream, JsonNode> parser = parser(type);

        try (InputStream in = connection.getInputStream()) {
            return parser.apply(in);
        } catch (IOException e) {
            throw new BootiqueException(1, "Config resource is not found or is inaccessible: " + url, e);
        }
    }

    Function<InputStream, JsonNode> parser(ParserType type) {

        Function<InputStream, JsonNode> parser = parsers.get(type);

        if (parser == null) {
            bootLogger.trace(() -> "No parser for type: " + type);
            throw new IllegalStateException("Can't find configuration parser for the format: " + type);
        }

        return parser;
    }

    ParserType parserTypeFromHeaders(URLConnection connection) {
        String contentType = connection.getHeaderField("Content-Type");
        if (contentType == null) {
            return null;
        }

        switch (contentType) {
            case "application/json":
                bootLogger.trace(() -> "Configuration is in JSON format (based on HTTP content-type)");
                return ParserType.JSON;
            default:
                return null;
        }
    }

    ParserType parserTypeFromExtension(URL url) {

        String path = url.getPath();
        if (path == null) {
            return null;
        }

        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) {
            return null;
        }

        switch (path.substring(dot + 1)) {
            case "yml":
            case "yaml":
                bootLogger.trace(() -> "Configuration is in YAML format (based on URL extension)");
                return ParserType.YAML;
            case "json":
                bootLogger.trace(() -> "Configuration is in JSON format (based on URL extension)");
                return ParserType.JSON;
            default:
                return null;
        }
    }
}
