package io.bootique.docs.testing;

import io.bootique.junit5.BQModuleTester;
import org.junit.jupiter.api.Test;

public class ValidityTest {

    // tag::Testing[]
    @Test
    public void autoLoadable() {
        BQModuleTester.of(MyModule.class)
                .testAutoLoadable()
                .testConfig();
    }
    // end::Testing[]
}
