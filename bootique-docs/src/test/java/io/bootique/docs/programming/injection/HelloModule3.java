package io.bootique.docs.programming.injection;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;

import javax.inject.Singleton;

// tag::binding[]
public class HelloModule3 implements BQModule {

    @Singleton
    @Provides
    Hello provideHello(UserNameService nameService) {  // <1>
        return new HelloService3(nameService);
    }

    @Override
    public void configure(Binder binder) { // <2>
    }
}
// end::binding[]

