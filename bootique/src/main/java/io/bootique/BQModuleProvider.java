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

import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;

import java.util.Collection;
import java.util.Collections;

/**
 * A provider of a DI module of a given kind. Central to Bootique module autoloading and metadata discovery.
 * Autoloaded modules supply "META-INF/services/io.bootique.BQModuleProvider" file packaged in the .jar containing
 * the name of the provider. Note that modules themselves can be their own providers by implementing this interface.
 *
 * @see Bootique#autoLoadModules()
 */
public interface BQModuleProvider {

    /**
     * Creates and returns a new instance of {@link BuiltModule}. Subclasses should override this method with their own
     * module logic, using a builder provided by {@link BuiltModule#of(BQModule)}.
     *
     * @see BuiltModule.Builder
     * @since 3.0
     */
    BuiltModule buildModule();

    /**
     * Returns a collection of providers of modules on which this provider's module depends. Concrete providers can
     * optionally define dependencies on other modules through this method. This allows to load app modules with
     * dependencies without relying on autoloading.
     *
     * @return collection of bootique module providers on which the current module depends.
     */
    default Collection<BQModuleProvider> dependencies() {
        return Collections.emptyList();
    }
}
