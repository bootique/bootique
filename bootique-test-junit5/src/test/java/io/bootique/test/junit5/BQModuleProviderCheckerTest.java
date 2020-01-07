package io.bootique.test.junit5;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BQModuleProviderCheckerTest {

    @Test
    public void testMatchingProvider() {
        BQModuleProvider p = new BQModuleProviderChecker(P1.class).matchingProvider();

        assertNotNull(p);
        assertTrue(p instanceof P1);
    }

    @Test
    public void testTestMetadata() {
        new BQModuleProviderChecker(P1.class).testMetadata();
    }

    public static class P1 implements BQModuleProvider {

        @Override
        public Module module() {
            return b -> {
            };
        }
    }

}
