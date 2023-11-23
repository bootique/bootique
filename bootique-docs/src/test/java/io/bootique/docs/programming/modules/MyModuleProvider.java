package io.bootique.docs.programming.modules;


import io.bootique.BQModuleProvider;
import io.bootique.bootstrap.BuiltModule;

// tag::MyModuleProvider[]
public class MyModuleProvider implements BQModuleProvider {

    @Override
    public BuiltModule buildModule() {
        return BuiltModule.of(new MyModule()).provider(this).build();
    }
}
// end::MyModuleProvider[]