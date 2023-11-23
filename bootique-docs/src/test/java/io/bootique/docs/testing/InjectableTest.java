package io.bootique.docs.testing;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class InjectableTest {

    // tag::Testing[]
    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app("--config=src/test/resources/my.yml")
            // end::Testing[]
            .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class))
            // tag::Testing[]
            .createRuntime();

    @Test
    public void service() {
        MyService service = app.getInstance(MyService.class);
        assertEquals("xyz", service.someMethod());
    }
    // end::Testing[]

}
