package io.bootique.docs.programming;

import io.bootique.BaseModule;
import io.bootique.Bootique;

// tag::Application[]
public class Application extends BaseModule {

    public static void main(String[] args) {
        Bootique.app(args) // <1>
                .autoLoadModules() // <2>
                .exec() // <3>
                .exit(); // <4>
    }
}
// end::Application[]

