package io.bootique.docs.programming;

import io.bootique.BQModule;
import io.bootique.Bootique;
import io.bootique.di.Binder;
import io.bootique.docs.programming.modules.service.MyService;
import io.bootique.docs.programming.modules.service.MyServiceImpl;

public class Modules {


    public void explicitModules() {

        // tag::explicitModules[]
        Bootique.app()
                .module(JettyModule.class) // <1>
                .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class)) // <2>
                .exec();
        // end::explicitModules[]
    }

    public void autoLoadableModules() {

        // tag::autoLoadableModules[]
        Bootique.app()
                .autoLoadModules() // <1>
                .module(binder -> binder.bind(MyService.class).to(MyServiceImpl.class)) // <2>
                .exec();
        // end::autoLoadableModules[]
    }

    static class JettyModule implements BQModule {
        @Override
        public void configure(Binder binder) {

        }
    }
}
