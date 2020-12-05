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
import io.bootique.config.jackson.merger.InPlacePropertiesMerger;
import io.bootique.env.Environment;

import javax.inject.Inject;
import java.util.Map;

/**
 * @since 2.0.B1
 */
public class PropertiesConfigurationLoader implements JsonConfigurationLoader {

    public static final int ORDER = DIPostConfigurationLoader.ORDER + 10;

    private final Environment environment;

    @Inject
    public PropertiesConfigurationLoader(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public JsonNode updateConfiguration(JsonNode mutableInput) {
        Map<String, String> properties = environment.frameworkProperties();
        return new InPlacePropertiesMerger(properties).apply(mutableInput);
    }
}
