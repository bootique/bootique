package io.bootique.docs.programming.modules;


import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;

// tag::MyModuleProvider[]
public class MyModuleProvider implements BQModuleProvider {

    @Override
    public ModuleCrate moduleCrate() {
        return ModuleCrate.of(new MyModule()).provider(this).build();
    }
}
// end::MyModuleProvider[]