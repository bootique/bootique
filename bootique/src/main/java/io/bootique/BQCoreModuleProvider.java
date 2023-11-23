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
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 3.0
 */
public class BQCoreModuleProvider implements BQModuleProvider {

    private final BQCoreModule module;

    public BQCoreModuleProvider(
            String[] args,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Supplier<Collection<BuiltModule>> modulesSource) {

        // BQCoreModule requires a couple of explicit services that can not be initialized within the module itself
        this.module = new BQCoreModule(args, bootLogger, shutdownManager, modulesSource);
    }

    /**
     * @since 3.0
     */
    @Override
    public BuiltModule buildModule() {
        return BuiltModule.of(module)
                .provider(this)
                .providerName("Bootique")
                .description("The core of Bootique runtime.").build();
    }
}
