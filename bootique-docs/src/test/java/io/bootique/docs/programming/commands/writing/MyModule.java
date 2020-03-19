package io.bootique.docs.programming.commands.writing;

import io.bootique.BQCoreModule;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;

// tag::Commands[]
public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addCommand(MyCommand.class);
    }
}
// end::Commands[]

