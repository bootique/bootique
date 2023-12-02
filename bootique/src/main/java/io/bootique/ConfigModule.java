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

package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.names.ClassToName;
import io.bootique.type.TypeRef;

import java.util.Objects;

/**
 * A superclass of Bootique DI modules that automatically builds a String prefix for resolving Module-specific
 * configuration based on the module class name. So e.g. if "MyModule" extends "ConfigModule", it will resolve module
 * configs under the "my" prefix. To use this functionality subclasses need to call one of the "config" methods, e.g.
 * {@link #config(Class, ConfigurationFactory)}.
 *
 * @see BaseModule
 */
public abstract class ConfigModule implements BQModule {

    protected static ClassToName CONFIG_PREFIX_BUILDER = ClassToName
            .builder()
            .convertToLowerCase()
            .stripSuffix("Module")
            .build();

    protected String configPrefix;

    protected ConfigModule() {
        init(defaultConfigPrefix());
    }

    protected ConfigModule(String configPrefix) {
        init(configPrefix);
    }

    private void init(String configPrefix) {
        Objects.requireNonNull(configPrefix);
        this.configPrefix = configPrefix;
    }

    /**
     * Does nothing and is intended for optional overriding.
     */
    @Override
    public void configure(Binder binder) {
        // do nothing
    }

    /**
     * @since 1.1
     */
    protected <T> T config(Class<T> type, ConfigurationFactory configurationFactory) {
        return configurationFactory.config(type, getConfigPrefix());
    }

    /**
     * @since 1.1
     */
    protected <T> T config(TypeRef<? extends T> type, ConfigurationFactory configurationFactory) {
        return configurationFactory.config(type, getConfigPrefix());
    }

    /**
     * @since 1.1
     */
    protected String getConfigPrefix() {
        return configPrefix;
    }

    protected String defaultConfigPrefix() {
        return CONFIG_PREFIX_BUILDER.toName(getClass());
    }

}
