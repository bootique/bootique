package io.bootique.docs.programming;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cli.Cli;


public class Runtime {

    public void getInstance() {

        BQRuntime runtime = Bootique.app()
                .autoLoadModules()
                .createRuntime();

// tag::getInstance[]
        Cli cli = runtime.getInstance(Cli.class);
        // end::getInstance[]
    }
}


