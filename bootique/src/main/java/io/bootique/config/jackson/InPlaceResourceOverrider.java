/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URL;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from one configuration resource.
 *
 * @since 0.24
 */
public class InPlaceResourceOverrider implements Function<JsonNode, JsonNode> {

    private URL source;
    private Function<URL, Optional<JsonNode>> parser;
    private BinaryOperator<JsonNode> merger;

    public InPlaceResourceOverrider(URL source, Function<URL, Optional<JsonNode>> parser, BinaryOperator<JsonNode> merger) {
        this.source = source;
        this.parser = parser;
        this.merger = merger;
    }

    @Override
    public JsonNode apply(JsonNode jsonNode) {
        return parser.apply(source)
                .map(configNode -> merger.apply(jsonNode, configNode))
                .orElse(jsonNode);
    }
}
