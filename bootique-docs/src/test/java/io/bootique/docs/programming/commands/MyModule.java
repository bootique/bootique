package io.bootique.docs.programming.commands;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.di.Binder;

// tag::addCommand[]
public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addCommand(MyCommand.class);
    }
}
// end::addCommand[]

