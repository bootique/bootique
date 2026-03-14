package io.bootique.docs.testing;

import io.bootique.junit.BQModuleTester;
import org.junit.jupiter.api.Test;

public class BQModuleTest {

    // tag::test[]
    @Test
    public void autoLoadable() {
        BQModuleTester.of(MyModule.class)
                .testAutoLoadable()
                .testConfig();
    }
    // end::test[]
}
