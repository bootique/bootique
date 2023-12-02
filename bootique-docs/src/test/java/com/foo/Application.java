// tag::Application[]
// tag::Main[]
package com.foo;

import io.bootique.Bootique;
// end::Main[]
import io.bootique.BQModule;
import io.bootique.jersey.JerseyModule;
// tag::Main[]

public class Application {

    public static void main(String[] args) {
        // end::Main[]

        BQModule module = binder ->
                JerseyModule.extend(binder).addResource(HelloResource.class);
        // tag::Main[]
        Bootique
                .app(args)
                // end::Main[]
                .module(module)
                // tag::Main[]
                .autoLoadModules()
                .exec()
                .exit();
    }
}
// end::Main[]
// end::Application[]
