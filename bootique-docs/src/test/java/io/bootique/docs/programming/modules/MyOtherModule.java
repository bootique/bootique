package io.bootique.docs.programming.modules;

import io.bootique.BaseModule;
import io.bootique.di.Binder;
import io.bootique.docs.programming.configuration.MyService;
import io.bootique.docs.programming.configuration.yaml.MyServiceImpl;

// tag::MyOtherModule[]
public class MyOtherModule extends BaseModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(MyService.class).to(MyServiceImpl.class);
    }
}
// end::MyOtherModule[]