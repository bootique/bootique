package io.bootique.docs.programming.modules;


import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;

// tag::MyModuleProvider[]
public class MyModuleProvider implements BQModuleProvider {
    @Override
    public BQModule module() {
        return new MyModule();
    }
}
// end::MyModuleProvider[]