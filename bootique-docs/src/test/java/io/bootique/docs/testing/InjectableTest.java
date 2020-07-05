package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class InjectableTest {

    @BQTestTool
    public BQTestFactory testFactory = new BQTestFactory();

    // tag::Testing[]
    @Test
    public void testService() {

        BQRuntime runtime = testFactory.app("--config=src/test/resources/my.yml")
                // end::Testing[]
                .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
                // tag::Testing[]
                .createRuntime();

        MyService service = runtime.getInstance(MyService.class);
        assertEquals("xyz", service.someMethod());
    }
    // end::Testing[]

}
