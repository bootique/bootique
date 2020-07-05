package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// tag::Testing[]
@BQTest
public class MyTest {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();
    // end::Testing[]


    // tag::TestAbc1[]
    @Test
    public void testAbc() {

        BQRuntime runtime = testFactory.app()
                // ensure all classpath modules are included
                .autoLoadModules()
                // add an adhoc module specific to the test
                .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
                .createRuntime();
        // ...
    }
// end::TestAbc1[]


    @Disabled("No real Jersey module available")
// tag::TestAbc2[]
    @Test
    public void testABC() {

        CommandOutcome result = testFactory.app("--server")
                .autoLoadModules()
                .run();
        // ...
    }
// end::TestAbc2[]

// tag::Testing[]
}
// end::Testing[]

