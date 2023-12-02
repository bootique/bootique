package io.bootique.docs.programming.modules.using;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.di.Binder;

// tag::MyModule[]
public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addCommand(MyCommand.class);
    }
}
// end::MyModule[]

