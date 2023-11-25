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
import io.bootique.config.jackson.merger.JsonConfigurationMerger;
import io.bootique.config.jackson.parser.JsonConfigurationParser;
import io.bootique.log.BootLogger;
import io.bootique.resource.ResourceFactory;

import java.net.URL;
import java.util.Collection;
import java.util.Objects;

/**
 * Configuration loader for a set of config URLs. Subclasses define where the URLs come from and the order of
 * priority for a particular loader.
 *
 * @since 2.0
 */
public abstract class UrlConfigurationLoader implements JsonConfigurationLoader {

    private final BootLogger bootLogger;
    private final JsonConfigurationParser parser;
    private final JsonConfigurationMerger merger;
    private final Collection<String> locations;

    protected UrlConfigurationLoader(
            BootLogger bootLogger,
            JsonConfigurationParser parser,
            JsonConfigurationMerger merger,
            Collection<String> locations) {

        this.bootLogger = Objects.requireNonNull(bootLogger);
        this.parser = Objects.requireNonNull(parser);
        this.merger = Objects.requireNonNull(merger);
        this.locations = Objects.requireNonNull(locations);
    }

    @Override
    public JsonNode updateConfiguration(JsonNode mutableInput) {
        return locations.stream()
                .map(this::toURL)
                .map(parser::parse)
                .filter(n -> n != null) // is there ever a condition when the parser returns null?
                .reduce(mutableInput, merger);
    }

    protected URL toURL(String location) {
        bootLogger.trace(() -> "Reading configuration at " + location);
        return new ResourceFactory(location).getUrl();
    }
}
