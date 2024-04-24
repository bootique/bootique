package io.bootique.docs.programming;

import io.bootique.Bootique;

public class Application1 {

    // tag::Application[]
    public static void main(String[] args) {
        Bootique.app(args) // <1>
                .autoLoadModules() // <2>
                .exec() // <3>
                .exit(); // <4>
    }
    // end::Application[]
}
