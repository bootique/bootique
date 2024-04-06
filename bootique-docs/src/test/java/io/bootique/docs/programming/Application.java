package io.bootique.docs.programming;

import io.bootique.BQModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.di.Binder;

// tag::Application[]
public class Application implements BQModule {

    public static void main(String[] args) {
        
        BQRuntime runtime = Bootique.app(args) // <1>
                .autoLoadModules() // <2>
                .createRuntime(); // <3>

        runtime.run() // <4>
                .exit(); // <5>
    }

    @Override
    public void configure(Binder binder) {
        // configure services
    }
}
// end::Application[]

