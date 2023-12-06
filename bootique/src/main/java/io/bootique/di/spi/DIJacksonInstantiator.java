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

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;

import javax.inject.Provider;

/**
 * A Jackson "instantiator" of a given Java type that creates objects and performs injection with Bootique DI providers.
 *
 * @since 3.0
 */
class DIJacksonInstantiator extends StdValueInstantiator {

    private final Provider<?> provider;

    public DIJacksonInstantiator(
            DeserializationConfig config,
            JavaType valueType,
            Provider<?> provider) {

        super(config, valueType);
        this.provider = provider;
    }

    @Override
    public boolean canCreateUsingDefault() {
        return true;
    }

    @Override
    public Object createUsingDefault(DeserializationContext ctxt) {
        return provider.get();
    }
}
