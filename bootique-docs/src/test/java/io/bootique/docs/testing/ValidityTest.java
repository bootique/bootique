package io.bootique.docs.testing;

import io.bootique.test.junit5.BQModuleProviderChecker;
import org.junit.jupiter.api.Test;

public class ValidityTest {

    // tag::Testing[]
    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(MyModuleProvider.class);
    }
    // end::Testing[]
}
