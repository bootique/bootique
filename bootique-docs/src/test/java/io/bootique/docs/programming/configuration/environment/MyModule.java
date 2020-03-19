package io.bootique.docs.programming.configuration.environment;

import io.bootique.BQCoreModule;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;

// tag::MyModule[]
class MyModule implements BQModule {
    public void configure(Binder binder) {

        BQCoreModule.extend(binder)
                .declareVar("my.prop1", "P1")
                .declareVar("my.prop2", "P2");
    }
}

// end::MyModule[]

