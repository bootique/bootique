package io.bootique.test.junit;

import com.google.inject.Module;
import io.bootique.BQModuleId;
import io.bootique.BQModuleProvider;
import org.junit.Assert;
import org.junit.Test;

public class BQModuleProviderCheckerTest {

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

    public static class P1 implements BQModuleProvider {
        private final Module module = b -> {
        };

        @Override
        public Module module() {
            return module;
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(module);
        }
    }
}
