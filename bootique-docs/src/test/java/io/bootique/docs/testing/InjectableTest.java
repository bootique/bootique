package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InjectableTest {

    @RegisterExtension
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
