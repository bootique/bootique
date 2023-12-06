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
package io.bootique.di.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.config.jackson.JsonConfigurationFactory;
import io.bootique.jackson.JacksonService;

import java.util.Collection;

/**
 * A {@link JsonConfigurationFactory} that adds Bootique dependency injection to the Jackson-based configuration
 * loading process.
 *
 * @since 3.0
 */
public class DIJsonConfigurationFactory extends JsonConfigurationFactory {

    private final DIJacksonBridgeModule jacksonBridge;

    public static DIJsonConfigurationFactory of(
            JsonNode rootConfigNode,
            DefaultInjector injector,
            Collection<Class<?>> injectionEnabledTypes) {

        DIJacksonBridgeModule jacksonBridge = new DIJacksonBridgeModule(injector, injectionEnabledTypes);

        // create and manage the ObjectMapper instance internally, dynamically adding DI-based instantiators
        ObjectMapper mapper = injector.getInstance(JacksonService.class).newObjectMapper();
        mapper.registerModule(jacksonBridge);

        return new DIJsonConfigurationFactory(rootConfigNode, mapper, jacksonBridge);
    }

    protected DIJsonConfigurationFactory(
            JsonNode rootConfigNode,
            ObjectMapper objectMapper,
            DIJacksonBridgeModule jacksonBridge) {

        super(rootConfigNode, objectMapper);
        this.jacksonBridge = jacksonBridge;
    }

    @Override
    public <T> T config(Class<T> type, String prefix) {

        // injection into config roots should work regardless of the annotations presence, but config children would
        // require a @BQConfig to be injectable.
        // TODO: Is this too confusing? Should we alternatively check for @BQConfig and print a warning if it is absent?
        jacksonBridge.enableInjectionInto(type);

        return super.config(type, prefix);
    }
}
