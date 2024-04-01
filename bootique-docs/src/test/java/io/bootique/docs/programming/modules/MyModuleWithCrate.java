package io.bootique.docs.programming.modules;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.di.Binder;

public class MyModuleWithCrate implements BQModule {

    // tag::MyModuleWithCrate[]
    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Module that does something useful")
                .build();
    }
    // end::MyModuleWithCrate[]

    @Override
    public void configure(Binder binder) {
    }
}

