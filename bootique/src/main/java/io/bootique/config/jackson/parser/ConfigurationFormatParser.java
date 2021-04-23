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

import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for the specific configuration format parser.
 *
 * @since 2.0.B1
 */
public interface ConfigurationFormatParser {

    /**
     * Parse incoming configuration InputStream
     *
     * @param stream for the configuration resource, managed by the caller
     * @return parsed configuration as {@link JsonNode}
     */
    JsonNode parse(InputStream stream);

    /**
     * @param url of the configuration resource
     * @param contentType of the configuration resource or null if it's unavailable
     * @return should this configuration resource be processed by this parser
     */
    boolean shouldParse(URL url, String contentType);

}
