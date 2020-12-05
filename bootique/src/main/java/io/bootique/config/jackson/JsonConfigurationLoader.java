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
package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A source of Bootique app configuration. Multiple loaders are chained together to load configuration from
 * multiple sources via the {@link #load(Set)} method.
 *
 * @since 2.0.B1
 */
public interface JsonConfigurationLoader {

    static JsonNode load(Set<JsonConfigurationLoader> loaders) {

        List<JsonConfigurationLoader> ordered = new ArrayList<>(loaders);
        ordered.sort(Comparator.comparing(JsonConfigurationLoader::getOrder));

        JsonNode root = new ObjectNode(new JsonNodeFactory(true));
        for (JsonConfigurationLoader loader : ordered) {
            root = loader.updateConfiguration(root);
        }

        return root;
    }


    /**
     * Returns an int that designates a relative order of this loader in the loaders sequence.
     */
    int getOrder();

    JsonNode updateConfiguration(JsonNode mutableInput);
}
