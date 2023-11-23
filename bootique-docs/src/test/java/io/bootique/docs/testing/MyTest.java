package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// tag::BQTest[]
@BQTest
public class MyTest {
// end::BQTest[]

    // tag::TestFactory[]
    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();
    // end::TestFactory[]


    @Disabled("Fake command, will fail")
    // tag::TestAbc[]
    @Test
    public void abc() {

        CommandOutcome result = testFactory.app("--server")
                // ensure all classpath modules are included
                .autoLoadModules()
                // add an adhoc module specific to the test
                .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
                .run();
        // ...
    }
// end::TestAbc[]


// tag::TestXyz[]
    @Test
    public void xyz() {

        BQRuntime app = testFactory.app("--server")
                .autoLoadModules()
                .createRuntime();
        // ...
    }
// end::TestXyz[]

// tag::BQTest[]
}
// end::BQTest[]

