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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A helper that orchestrates configuration loading.
 *
 * @since 0.17
 */
public class JsonNodeConfigurationBuilder {

    private Supplier<Stream<URL>> resourceStreamSupplier;
    private Function<URL, Optional<JsonNode>> parser;
    private BinaryOperator<JsonNode> merger;
    private Function<JsonNode, JsonNode> overrider;

    protected JsonNodeConfigurationBuilder() {
    }

    public static JsonNodeConfigurationBuilder builder() {
        return new JsonNodeConfigurationBuilder();
    }

    public JsonNodeConfigurationBuilder resources(Supplier<Stream<URL>> streamSupplier) {
        this.resourceStreamSupplier = streamSupplier;
        return this;
    }

    public JsonNodeConfigurationBuilder overrider(Function<JsonNode, JsonNode> overrider) {
        this.overrider = overrider;
        return this;
    }

    public JsonNodeConfigurationBuilder parser(Function<URL, Optional<JsonNode>> parser) {
        this.parser = parser;
        return this;
    }

    public JsonNodeConfigurationBuilder merger(BinaryOperator<JsonNode> merger) {
        this.merger = merger;
        return this;
    }

    public JsonNode build() {

        Objects.requireNonNull(resourceStreamSupplier);
        Objects.requireNonNull(parser);
        Objects.requireNonNull(merger);

        JsonNode rootNode;

        try (Stream<URL> sources = resourceStreamSupplier.get()) {
            rootNode = sources
                    .map(parser::apply)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(merger)
                    .orElseGet(() -> new ObjectNode(new JsonNodeFactory(true)));
        }

        return overrider.apply(rootNode);
    }
}
