package io.bootique.docs.programming.modules;


import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

// tag::MyModuleProvider[]
public class MyModuleProvider implements BQModuleProvider {

    @Override
    public BQModule module() {
        return new MyModule();
    }
}
// end::MyModuleProvider[]