package io.bootique.test.junit;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A helper class to for writing generic tests for {@link BQModuleProvider} implementors. It allows to verify that the
 * provider and related classes are wired properly, there are no typos in service descriptors, etc. Same usage:
 * <pre>
 * &#64;Test
 * public void testPresentInJar() {
 * 	   BQModuleProviderChecker.testPresentInJar(MyModuleProvider.class);
 * }
 * </pre>
 *
 * @since 0.15
 */
public class BQModuleProviderChecker {

    private Class<? extends BQModuleProvider> provider;

    protected BQModuleProviderChecker(Class<? extends BQModuleProvider> provider) {
        this.provider = Objects.requireNonNull(provider);
    }

    /**
     * @param provider provider type that we are testing.
     */
    public static void testPresentInJar(Class<? extends BQModuleProvider> provider) {
        new BQModuleProviderChecker(provider).testPresentInJar();
    }

    public static void testModulesLoaded(BQRuntime bqRuntime, List<Class<? extends Module>> moduleList) {
        final ModulesMetadata modulesMetadata = bqRuntime.getInstance(ModulesMetadata.class);

        final List<String> actualModules = modulesMetadata
                .getModules()
                .stream()
                .map(ModuleMetadata::getName)
                .collect(toList());

        final String[] expectedModules = moduleList
                .stream()
                .map(Class::getSimpleName)
                .toArray(String[]::new);

        // Using class names for checking module existing - weak.
        assertThat(actualModules, hasItems(expectedModules));
    }

    /**
     * Checks that config metadata for the Module created by tested provider can be loaded without errors. Does not
     * verify the actual metadata contents.
     *
     * @param provider provider type that we are testing.
     * @since 0.21
     */
    public static void testMetadata(Class<? extends BQModuleProvider> provider) {
        new BQModuleProviderChecker(provider).testMetadata();
    }

    protected Stream<BQModuleProvider> matchingProviders() {
        return StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
                .filter(p -> p != null && provider.equals(p.getClass()));
    }

    protected BQModuleProvider matchingProvider() {
        return matchingProviders().findFirst().get();
    }

    protected void testPresentInJar() {
        Long c = matchingProviders().collect(counting());

        switch (c.intValue()) {
            case 0:
                fail("Expected provider '" + provider.getName() + "' is not found");
                break;
            case 1:
                break;
            default:
                fail("Expected provider '" + provider.getName() + "' is found more then once: " + c);
                break;
        }
    }

    protected void testMetadata() {

        testWithFactory(testFactory -> {
            // must auto-load modules to ensure all tested module dependencies are present...
            BQRuntime runtime = testFactory.app().autoLoadModules().createRuntime();

            BQModuleProvider provider = matchingProvider();
            String providerName = provider.name();

            // loading metadata ensures that all annotations are properly applied...
            Optional<ModuleMetadata> moduleMetadata = runtime
                    .getInstance(ModulesMetadata.class)
                    .getModules()
                    .stream()
                    .filter(mmd -> providerName.equals(mmd.getProviderName()))
                    .findFirst();

            assertTrue("No module metadata available for provider: '" + providerName + "'", moduleMetadata.isPresent());
            moduleMetadata.get().getConfigs();
        });
    }

    protected void testWithFactory(Consumer<BQTestFactory> test) {
        BQTestFactory testFactory = new BQTestFactory();
        try {
            testFactory.before();
            test.accept(testFactory);
        } finally {
            testFactory.after();
        }
    }
}
