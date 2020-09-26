package io.bootique.docs.programming.modules;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.docs.programming.modules.service.MyService;
import io.bootique.docs.programming.modules.service.MyServiceImpl;

// tag::MyModule[]
public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(MyService.class).to(MyServiceImpl.class);
    }
}
// end::MyModule[]
