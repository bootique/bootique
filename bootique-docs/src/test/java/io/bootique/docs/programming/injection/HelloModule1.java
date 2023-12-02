package io.bootique.docs.programming.injection;

import io.bootique.BQModule;
import io.bootique.di.Binder;

public class HelloModule1 implements BQModule {

    // tag::binding[]
    @Override
    public void configure(Binder binder) {
        binder.bind(Hello.class)
                .to(HelloService2.class)
                .inSingletonScope();
    }
    // end::binding[]
}

