package io.bootique.test.junit;

import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.counting;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A helper class to simplify writing the tests that need to check that a
 * specified {@link BQModuleProvider} is available via ServiceLoader mechanism.
 * This is a useful test as
 * <code>META-INF/services/BQModuleProvider</code> files can
 * contain typos or omissions. Sample usage:
 * <p>
 * <pre>
 * &#64;Test
 * public void testPresentInJar() {
 * 	BQModuleProviderChecker.testPresentInJar(MyModuleProvider.class);
 * }
 * </pre>
 *
 * @since 0.15
 */
// TODO: combine this in some kind of test suite for providers?
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
            BQRuntime runtime = testFactory.app().autoLoadModules().createRuntime().getRuntime();

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
