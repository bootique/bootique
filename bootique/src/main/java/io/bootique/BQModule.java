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

import io.bootique.di.Binder;

/**
 * Represents a unit of configuration of the Bootique DI container.
 */
@FunctionalInterface
public interface BQModule {

    /**
     * Returns a "crate" wrapping this module. The returned crate is used by Bootique to bootstrap the module within the
     * runtime. To supply extra module metadata, modules should override the default implementation using the builder
     * provided by {@link ModuleCrate#of(BQModule)}.
     *
     * @see ModuleCrate#of(BQModule)
     * @since 3.0
     */
    default ModuleCrate crate() {
        return ModuleCrate.of(this).build();
    }

    /**
     * A callback method invoked during injector assembly that allows the module to load its services.
     *
     * @param binder a binder object passed by the injector assembly environment.
     */
    void configure(Binder binder);
}
