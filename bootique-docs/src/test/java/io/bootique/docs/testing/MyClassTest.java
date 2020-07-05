package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;

// tag::Testing[]
@BQTest
public class MyClassTest {

    @BQTestTool
    final static BQTestFactory testFactory = new BQTestFactory();
    // end::Testing[]

    @BQApp
    private static BQRuntime app = testFactory.app()
            // ensure all classpath modules are included
            .autoLoadModules()
            // add an adhoc module specific to the test
            .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
            .createRuntime();

// tag::Testing[]
}
// end::Testing[]


