package io.bootique.docs.programming.commands.custom;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;

public class MyDaemonCommand extends CommandWithMetadata {

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(MyDaemonCommand.class)
                .description("My command does something important.")
                .build();
    }

    public MyDaemonCommand() {
        super(createMetadata());
    }

    // tag::run[]
    @Override
    public CommandOutcome run(Cli cli) {

        // ... start some process in a different thread ....

        // now wait till the app is stopped from another thread
        // or the JVM is terminated
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // ignore exception or log if needed
        }

        return CommandOutcome.succeeded();
    }
    // end::run[]
}

