package io.bootique.docs.programming;

import io.bootique.BQRuntime;
import io.bootique.Bootique;

public class Application2  {

    // tag::Application[]
    public static void main(String[] args) {
        BQRuntime runtime = Bootique.app(args)
                .autoLoadModules()
                .createRuntime(); // <1>

        runtime.run() // <2>
                .exit();
    }
    // end::Application[]
}
