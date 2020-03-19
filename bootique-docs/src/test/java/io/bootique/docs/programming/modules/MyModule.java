package io.bootique.docs.programming.modules;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.docs.programming.configuration.MyService;
import io.bootique.docs.programming.configuration.yaml.MyServiceImpl;

// tag::MyModule[]
public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(MyService.class).to(MyServiceImpl.class);
    }
}
// end::MyModule[]
