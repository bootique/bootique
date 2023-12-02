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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A provider of a DI module of a given kind. Central to Bootique module autoloading and metadata discovery.
 * Autoloaded modules supply "META-INF/services/io.bootique.BQModuleProvider" file packaged in the .jar containing
 * the name of the provider. Note that modules themselves can be their own providers by implementing this interface.
 *
 * @see Bootique#autoLoadModules()
 * @deprecated now modules are their own providers (including auto-loading) and this type is irrelevant. Though it is
 * still supported until Bootique 4.0.
 */
@Deprecated(since = "3.0", forRemoval = true)
public interface BQModuleProvider {

    /**
     * Creates and returns a DI module specific to this provider.
     */
    BQModule module();

    /**
     * Returns a new instance of {@link ModuleCrate.Builder} initialized with module for this provider.
     * Subclasses can invoke extra builder methods to provide metadata, etc.
     *
     * @return a new instance of {@link ModuleCrate} builder specific to this provider.
     */
    default BQModuleMetadata.Builder moduleBuilder() {
        return BQModuleMetadata
                .builder(module())
                .overrides(overrides())
                .providerName(name())
                .configs(configs());
    }

    /**
     * A potentially empty map of configuration types supported by this module, keyed by default configuration
     * prefix.
     *
     * @return a potentially empty map of configuration types supported by this module, keyed by default configuration
     * prefix.
     */
    default Map<String, Type> configs() {
        return Collections.emptyMap();
    }

    /**
     * Returns a potentially empty Collection with the types of the module
     * overridden by this Module.
     *
     * @return a potentially empty collection of modules overridden by the
     * Module created by this provider.
     */
    default Collection<Class<? extends BQModule>> overrides() {
        return Collections.emptyList();
    }

    /**
     * Returns a human-readable name of the provider.
     *
     * @return a human-readable name of the provider. Equals to the "simple" class name by default.
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * Returns a collection of providers of modules on which this provider's module depends. Concrete providers can
     * optionally define dependencies on other modules through this method. This allows to load app modules with
     * dependencies without relying on auto-loading.
     *
     * @return collection of bootique module providers on which the current module depends.
     * @deprecated in 3.0 this information will be ignored, and in the absence of auto-loading,  any dependencies will
     * need to be added manually.
     */
    default Collection<BQModuleProvider> dependencies() {
        return Collections.emptyList();
    }
}
