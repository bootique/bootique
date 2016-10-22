package io.bootique.module;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModuleMetadataIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testDefault() {
        BQRuntime runtime = runtimeFactory.app().createRuntime();
        ModulesMetadata md = runtime.getInstance(ModulesMetadata.class);

        assertEquals("Expected BQCoreModule + 2 test modules", 3, md.getModules().size());

        Collection<String> names = md.getModules().stream().map(ModuleMetadata::getName).collect(toSet());
        assertEquals(3, names.size());
        assertTrue(names.contains("BQCoreModule"));
    }

    @Test
    public void testCustomModule() {
        BQRuntime runtime = runtimeFactory.app().module(b -> {
        }).createRuntime();
        ModulesMetadata md = runtime.getInstance(ModulesMetadata.class);
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
            public String moduleName(Class<?> moduleType) {
                return "mymodule";
            }
        }).createRuntime();

        ModulesMetadata md = runtime.getInstance(ModulesMetadata.class);
        assertEquals("Expected BQCoreModule + 2 test modules + custom module", 4, md.getModules().size());

        Collection<String> names = md.getModules().stream().map(ModuleMetadata::getName).collect(toSet());
        assertEquals(4, names.size());
        assertTrue(names.contains("mymodule"));
    }
}
