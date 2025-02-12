package io.bootique.docs.programming.injection;

import io.bootique.BQModule;
import io.bootique.di.Binder;

public class HelloModule2 implements BQModule {

    // tag::binding[]
    @Override
    public void configure(Binder binder) {
        binder.bind(Hello.class)
                .toJakartaProvider(HelloService3Provider.class)
                .inSingletonScope();
    }
    // end::binding[]
}

