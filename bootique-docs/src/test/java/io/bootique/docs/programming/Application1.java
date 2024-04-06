package io.bootique.docs.programming;

import io.bootique.BaseModule;
import io.bootique.Bootique;

public class Application1 extends BaseModule {

    // tag::Application[]
    public static void main(String[] args) {
        Bootique.app(args)
                .autoLoadModules()
                .exec()
                .exit();
    }
    // end::Application[]
}
