package io.bootique.docs.programming.application;

import io.bootique.BQModule;
import io.bootique.Bootique;
import io.bootique.di.Binder;

// tag::Application[]
public class Application implements BQModule {

    public static void main(String[] args) {
        Bootique.app(args)
                .autoLoadModules()
                .module(Application.class) // <1>
                .exec()
                .exit();
    }

    public void configure(Binder binder) {
        // load app-specific services; redefine standard ones
    }
}
// end::Application[]

