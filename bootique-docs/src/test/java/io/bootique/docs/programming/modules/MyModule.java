package io.bootique.docs.programming.modules;

import io.bootique.BQModule;
import io.bootique.di.Binder;

// tag::MyModule[]
public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(MyService.class).to(MyServiceImpl.class);
    }
}
// end::MyModule[]

interface MyService {

    void doSomething();
}

class MyServiceImpl implements MyService {

    @Override
    public void doSomething() {

    }
}
