package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQTestClassFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

// tag::Testing[]
public class MyClassTest {

    @RegisterExtension
    public static BQTestClassFactory testFactory = new BQTestClassFactory();
    // end::Testing[]

    private static BQRuntime sharedRuntime;

    @BeforeAll
    public static void initSharedRuntime() {
        sharedRuntime =  testFactory.app()
                // ensure all classpath modules are included
                .autoLoadModules()
                // add an adhoc module specific to the test
                .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
                .createRuntime();

    }
// tag::Testing[]
}
// end::Testing[]


