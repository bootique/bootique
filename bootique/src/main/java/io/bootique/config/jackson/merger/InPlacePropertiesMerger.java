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

package io.bootique.config.jackson.merger;

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.config.jackson.path.PathSegment;

import java.util.Map;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from a map of properties.
 */
public class InPlacePropertiesMerger implements Function<JsonNode, JsonNode> {

    private final Map<String, String> properties;

    public InPlacePropertiesMerger(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public JsonNode apply(JsonNode t) {

        // Sorting property names. The reason is array properties ("abc.xyz[1]")... If multiple entries are added to
        // array, and internally they are out of order, ArrayIndexOutOfBoundsException occurs

        // TODO: ordering is a hack. Sorting is done lexicographically, this will only work for the first 10 entries.
        properties.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {

            PathSegment<?> target = lastPathComponent(t, e.getKey());
            target.fillMissingParents();

            if (target.getParent() == null) {
                throw new IllegalArgumentException("No parent node");
            }

            target.getParent().writeChildValue(target.getIncomingPath(), e.getValue());
        });

        return t;
    }

    protected PathSegment<?> lastPathComponent(JsonNode t, String path) {
        return PathSegment.create(t, path).lastPathComponent().get();
    }
}
