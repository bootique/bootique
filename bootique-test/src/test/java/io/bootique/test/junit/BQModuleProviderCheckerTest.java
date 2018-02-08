package io.bootique.test.junit;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static java.util.Collections.singletonList;

public class BQModuleProviderCheckerTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testMatchingProvider() {
        BQModuleProvider p = new BQModuleProviderChecker(P1.class).matchingProvider();

        Assert.assertNotNull(p);
        Assert.assertTrue(p instanceof P1);
    }

    @Test
    public void testTestMetadata() {
        new BQModuleProviderChecker(P1.class).testMetadata();
    }

    @Test
    public void testTestModulesLoaded() {
        final BQRuntime runtime = testFactory.app().createRuntime();
        BQModuleProviderChecker.testModulesLoaded(runtime, singletonList(BQCoreModule.class));
    }

    @Test(expected = AssertionError.class)
    public void testTestModulesNotLoaded() {
        final BQRuntime runtime = testFactory.app().createRuntime();
        BQModuleProviderChecker.testModulesLoaded(runtime, singletonList(NonLoadedModule.class));
    }

    public static class P1 implements BQModuleProvider {

        @Override
        public Module module() {
            return b -> {
            };
        }
    }
}


class NonLoadedModule implements Module {

    @Override
    public void configure(Binder binder) {
    }
}
