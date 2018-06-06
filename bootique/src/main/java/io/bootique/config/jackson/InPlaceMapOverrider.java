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

import java.util.Map;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from a map of properties.
 *
 * @since 0.17
 */
public class InPlaceMapOverrider implements Function<JsonNode, JsonNode> {

    private Map<String, String> properties;

    public InPlaceMapOverrider(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public JsonNode apply(JsonNode t) {
        properties.entrySet().forEach(e -> {

            PathSegment target = lastPathComponent(t, e.getKey());
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
