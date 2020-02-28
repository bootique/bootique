package io.bootique.docs.programming.modules;


import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;

import java.util.Collection;
import java.util.Collections;

// tag::MyModuleProvider[]
// tag::MyModuleProviderMethods[]
public class MyModuleProvider implements BQModuleProvider {
    // end::MyModuleProviderMethods[]
    @Override
    public BQModule module() {
        return new MyModule();
    }
// end::MyModuleProvider[]

// tag::MyModuleProviderMethods[]
    // ...

    // provides human-readable name of the module
    @Override
    public String name() {
        return "CustomName";
    }

    // a collection of modules whose services are overridden by this module
    @Override
    public Collection<Class<? extends BQModule>> overrides() {
        return Collections.singleton(BQCoreModule.class);
    }
    // tag::MyModuleProvider[]
}
// end::MyModuleProviderMethods[]

// end::MyModuleProvider[]