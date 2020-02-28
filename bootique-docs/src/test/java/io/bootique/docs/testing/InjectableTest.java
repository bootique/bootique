package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InjectableTest {

    @Rule
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
