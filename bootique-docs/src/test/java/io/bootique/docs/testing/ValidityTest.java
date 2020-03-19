package io.bootique.docs.testing;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ValidityTest {

    // tag::Testing[]
    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(MyModuleProvider.class);
    }
    // end::Testing[]
}
