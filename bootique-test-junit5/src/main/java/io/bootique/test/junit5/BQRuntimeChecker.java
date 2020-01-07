package io.bootique.test.junit5;

import com.google.inject.Module;
import io.bootique.BQRuntime;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class BQRuntimeChecker {

    /**
     * Verifies that runtime contains expected modules.
     *
     * @param runtime         a Bootique runtime whose contents we are testing.
     * @param expectedModules a vararg array of expected module types.
     */
    @SafeVarargs
    public static void testModulesLoaded(BQRuntime runtime, Class<? extends Module>... expectedModules) {

        final ModulesMetadata modulesMetadata = runtime.getInstance(ModulesMetadata.class);

        final List<String> actualModules = modulesMetadata
                .getModules()
                .stream()
                .map(ModuleMetadata::getName)
                .collect(toList());

        final String[] expectedModuleNames = Stream.of(expectedModules)
                .map(Class::getSimpleName)
                .toArray(String[]::new);

        // Using class names for checking module existing - weak.
        assertThat(actualModules, hasItems(expectedModuleNames));
    }
}
