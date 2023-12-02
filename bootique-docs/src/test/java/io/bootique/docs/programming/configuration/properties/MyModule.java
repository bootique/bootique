package io.bootique.docs.programming.configuration.properties;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.di.Binder;

// tag::MyModule[]
class MyModule implements BQModule {
    public void configure(Binder binder) {

        BQCoreModule.extend(binder)
                .setProperty("bq.my.prop1", "valX")
                .setProperty("bq.my.prop2", "valY");
    }
}
// end::MyModule[]

