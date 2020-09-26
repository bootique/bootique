package io.bootique.docs.programming.modules;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.docs.programming.configuration.MyService;
import io.bootique.docs.programming.configuration.yaml.MyServiceImpl;


public class BindingModule implements BQModule {

    // tag::binding[]
    @Override
    public void configure(Binder binder) {
        binder.bind(MyService.class).to(MyServiceImpl.class).inSingletonScope(); // <1>
    }
    // end::binding[]
}

