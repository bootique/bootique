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

package io.bootique.meta.module;

import io.bootique.BQModuleMetadata;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleMetadataIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void testDefault() {
        ModulesMetadata md = appManager.runtime(Bootique.app()).getInstance(ModulesMetadata.class);

        assertEquals(1, md.getModules().size(), "Expected BQCoreModule");

        Optional<ModuleMetadata> coreMd = md.getModules()
                .stream()
                .filter(m -> "BQCoreModule".equals(m.getName()))
                .findFirst();
        assertTrue(coreMd.isPresent());
        assertEquals("The core of Bootique runtime.", coreMd.get().getDescription());
    }

    @Test
    public void testCustomModule() {
        ModulesMetadata md = appManager.runtime(Bootique.app()
                        .module(b -> {
                        }))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");
    }

    @Test
    public void testCustomNamedModule() {
        BQRuntime runtime = appManager.runtime(Bootique.app().moduleProvider(new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public BQModuleMetadata.Builder moduleBuilder() {
                return BQModuleProvider.super
                        .moduleBuilder()
                        .name("mymodule");
            }
        }));

        ModulesMetadata md = runtime.getInstance(ModulesMetadata.class);
        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> myMd = md.getModules()
                .stream()
                .filter(m -> "mymodule".equals(m.getName()))
                .findFirst();
        assertTrue(myMd.isPresent());
    }

    @Test
    public void testProvider() {
        ModulesMetadata md = appManager.runtime(Bootique.app()
                .moduleProvider(new M1Provider())).getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> m1Md = md.getModules()
                .stream()
                .filter(m -> "M1Module".equals(m.getName()))
                .findFirst();
        assertTrue(m1Md.isPresent());
    }

    static class M1Provider implements BQModuleProvider {

        @Override
        public BQModule module() {
            return new M1Module();
        }
    }

    static class M1Module implements BQModule {
        @Override
        public void configure(Binder binder) {
        }
    }
}
