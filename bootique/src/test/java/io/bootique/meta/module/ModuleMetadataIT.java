package io.bootique.meta.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleMetadataIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testDefault() {
        ModulesMetadata md = runtimeFactory.app().createRuntime().getInstance(ModulesMetadata.class);

        assertEquals("Expected BQCoreModule + 2 test modules", 3, md.getModules().size());

        Optional<ModuleMetadata> coreMd = md.getModules()
                .stream()
                .filter(m -> "BQCoreModule".equals(m.getName()))
                .findFirst();
        assertTrue(coreMd.isPresent());
        assertEquals("Bootique core module.", coreMd.get().getDescription());
    }

    @Test
    public void testCustomModule() {
        ModulesMetadata md = runtimeFactory.app()
                .module(b -> {
                })
                .createRuntime()
                .getInstance(ModulesMetadata.class);

        assertEquals("Expected BQCoreModule + 2 test modules + custom module", 4, md.getModules().size());
    }

    @Test
    public void testCustomNamedModule() {
        BQRuntime runtime = runtimeFactory.app().module(new BQModuleProvider() {
            @Override
            public Module module() {
                return b -> {
                };
            }

            @Override
            public BQModule.Builder moduleBuilder() {
                return BQModuleProvider.super
                        .moduleBuilder()
                        .name("mymodule");
            }
        }).createRuntime();

        ModulesMetadata md = runtime.getInstance(ModulesMetadata.class);
        assertEquals("Expected BQCoreModule + 2 test modules + custom module", 4, md.getModules().size());

        Optional<ModuleMetadata> myMd = md.getModules()
                .stream()
                .filter(m -> "mymodule".equals(m.getName()))
                .findFirst();
        assertTrue(myMd.isPresent());
    }

    @Test
    public void testProvider() {
        ModulesMetadata md = runtimeFactory.app()
                .module(new M1Provider())
                .createRuntime().getInstance(ModulesMetadata.class);

        assertEquals("Expected BQCoreModule + 2 test modules + custom module", 4, md.getModules().size());

        Optional<ModuleMetadata> m1Md = md.getModules()
                .stream()
                .filter(m -> "M1Module".equals(m.getName()))
                .findFirst();
        assertTrue(m1Md.isPresent());
    }

    static class M1Provider implements BQModuleProvider {

        @Override
        public Module module() {
            return new M1Module();
        }
    }

    static class M1Module implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }
}
