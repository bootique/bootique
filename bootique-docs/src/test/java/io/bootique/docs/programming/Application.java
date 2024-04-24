package io.bootique.docs.programming;

import io.bootique.BQModule;
import io.bootique.Bootique;
import io.bootique.di.Binder;

// tag::Application[]
public class Application implements BQModule {

    public static void main(String[] args) {
        Bootique.main(args);
    }

    @Override
    public void configure(Binder binder) {
        // configure services
    }
}
// end::Application[]

