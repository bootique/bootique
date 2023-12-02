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

import io.bootique.*;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @deprecated legacy metadata tests done via BQModuleProvider
 */
@Deprecated
public class ModuleMetadata_BQModuleProviderIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void provider() {
        ModulesMetadata md = appManager.runtime(Bootique.app()
                .moduleProvider(new M1Provider())).getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> m1Md = md.getModules()
                .stream()
                .filter(m -> "M1Module".equals(m.getName()))
                .findFirst();
        assertTrue(m1Md.isPresent());
    }

    @Test
    public void module_byType_selfProvider() {
        ModulesMetadata md = appManager
                .runtime(Bootique.app().module(M3ModuleAndProvider.class))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> cmd = md.getModules()
                .stream()
                .filter(m -> "N3".equals(m.getName()))
                .findFirst();
        assertTrue(cmd.isPresent());
    }

    @Test
    public void module_byInstance_selfProvider() {
        ModulesMetadata md = appManager
                .runtime(Bootique.app().module(new M3ModuleAndProvider()))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> cmd = md.getModules()
                .stream()
                .filter(m -> "N3".equals(m.getName()))
                .findFirst();
        assertTrue(cmd.isPresent());
    }

    @Test
    public void deprecatedViaAnnotation_ByType() {
        ModulesMetadata md = appManager
                .runtime(Bootique.app().module(M2Module.class))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> m2Md = md.getModules()
                .stream()
                .filter(m -> "M2Module".equals(m.getName()))
                .findFirst();
        assertTrue(m2Md.isPresent());
        assertTrue(m2Md.get().isDeprecated());
    }

    @Test
    public void deprecatedViaAnnotation_viaProvider() {
        ModulesMetadata md = appManager
                .runtime(Bootique.app().moduleProvider(new M2ImplicitlyDeprecatedProvider()))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> m2Md = md.getModules()
                .stream()
                .filter(m -> "M2Module".equals(m.getName()))
                .findFirst();
        assertTrue(m2Md.isPresent());
        assertTrue(m2Md.get().isDeprecated());
    }

    static class M1Module implements BQModule {
        @Override
        public void configure(Binder binder) {
        }
    }

    // Note to future self: this is not a real deprecation. The annotation is used to test the generated module metadata
    @Deprecated(since = "33.0", forRemoval = true)
    public static class M2Module implements BQModule {
        @Override
        public void configure(Binder binder) {
        }
    }

    public static class M3ModuleAndProvider implements BQModule, BQModuleProvider {

        @Override
        public BQModule module() {
            return this;
        }

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).moduleName("N3").build();
        }

        @Override
        public void configure(Binder binder) {
        }
    }


    static class M1Provider implements BQModuleProvider {

        @Override
        public BQModule module() {
            return new M1Module();
        }
    }

    static class M2ImplicitlyDeprecatedProvider implements BQModuleProvider {
        @Override
        public BQModule module() {
            return new M2Module();
        }
    }
}
