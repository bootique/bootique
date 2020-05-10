package io.bootique.docs.programming.commands.writing;

import io.bootique.BQCoreModule;
import io.bootique.BaseModule;
import io.bootique.di.Binder;

// tag::Commands[]
public class MyModule extends BaseModule {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addCommand(MyCommand.class);
    }
}
// end::Commands[]

