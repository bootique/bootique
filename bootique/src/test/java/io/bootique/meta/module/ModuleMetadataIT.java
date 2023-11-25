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
import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleMetadataIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void basic() {
        ModulesMetadata allModules = appManager.runtime(Bootique.app()).getInstance(ModulesMetadata.class);

        assertEquals(1, allModules.getModules().size(), "Expected BQCoreModule");
        ModuleMetadata cmd = allModules.getModules().iterator().next();

        assertEquals("BQCoreModule", cmd.getName());
        assertEquals(BQCoreModule.class, cmd.getType());
        assertEquals("The core of Bootique runtime.", cmd.getDescription());
        assertFalse(cmd.isDeprecated());
    }

    @Test
    public void customModule() {
        ModulesMetadata md = appManager.runtime(Bootique.app()
                        .module(b -> {
                        }))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");
    }

    @Test
    public void customNamedModule() {

        BQModule m = b -> {
        };
        BQModuleProvider provider = () -> BuiltModule.of(m).moduleName("mymodule").build();
        BQRuntime runtime = appManager.runtime(Bootique.app().moduleProvider(provider));

        ModulesMetadata allModules = runtime.getInstance(ModulesMetadata.class);
        assertEquals(2, allModules.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> md = allModules.getModules()
                .stream()
                .filter(mdl -> "mymodule".equals(mdl.getName()))
                .findFirst();
        assertTrue(md.isPresent());

        assertEquals(m.getClass(), md.get().getType());
        assertNull(md.get().getDescription());
        assertFalse(md.get().isDeprecated());
    }

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
        assertEquals("P3", cmd.get().getProviderName());
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
        assertEquals("P3", cmd.get().getProviderName());
    }

    @Test
    public void deprecatedViaProviderMetadata() {
        ModulesMetadata md = appManager
                .runtime(Bootique.app().moduleProvider(new M1DeprecatedProvider()))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> m1Md = md.getModules()
                .stream()
                .filter(m -> "M1Module".equals(m.getName()))
                .findFirst();
        assertTrue(m1Md.isPresent());
        assertTrue(m1Md.get().isDeprecated());
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

    @Test
    public void deprecatedViaAnnotation_UndeprecatedViaProvider() {
        ModulesMetadata md = appManager
                .runtime(Bootique.app().moduleProvider(new M2UndeprecatedProvider()))
                .getInstance(ModulesMetadata.class);

        assertEquals(2, md.getModules().size(), "Expected BQCoreModule + custom module");

        Optional<ModuleMetadata> m2Md = md.getModules()
                .stream()
                .filter(m -> "M2Module".equals(m.getName()))
                .findFirst();
        assertTrue(m2Md.isPresent());
        assertFalse(m2Md.get().isDeprecated());
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
        public BuiltModule buildModule() {
            return BuiltModule.of(this).moduleName("N3").providerName("P3").build();
        }

        @Override
        public void configure(Binder binder) {
        }
    }


    static class M1Provider implements BQModuleProvider {

        @Override
        public BuiltModule buildModule() {
            return BuiltModule.of(new M1Module()).provider(this).build();
        }
    }

    static class M1DeprecatedProvider implements BQModuleProvider {

        @Override
        public BuiltModule buildModule() {
            return BuiltModule.of(new M1Module()).provider(this).deprecated(true).build();
        }
    }

    static class M2ImplicitlyDeprecatedProvider implements BQModuleProvider {
        @Override
        public BuiltModule buildModule() {
            return BuiltModule.of(new M2Module()).provider(this).build();
        }
    }

    static class M2UndeprecatedProvider implements BQModuleProvider {
        @Override
        public BuiltModule buildModule() {
            return BuiltModule.of(new M2Module()).provider(this).deprecated(false).build();
        }
    }
}
