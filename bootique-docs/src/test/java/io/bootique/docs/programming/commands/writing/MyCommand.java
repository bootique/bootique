package io.bootique.docs.programming.commands.writing;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;

// tag::Commands[]
public class MyCommand extends CommandWithMetadata {

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(MyCommand.class)
                .description("My command does something important.")
                .build();
    }

    public MyCommand() {
        super(createMetadata());
    }

    // tag::CommandsRun[]
    @Override
    public CommandOutcome run(Cli cli) {
        // end::CommandsRun[]

        // ... run the command here....

        // end::Commands[]

        // tag::CommandsRun[]

        // ... start some process in a different thread ....

        // now wait till the app is stopped from another thread
        // or the JVM is terminated
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // ignore exception or log if needed
        }

        // end::CommandsRun[]


        // tag::Commands[]
        // tag::CommandsRun[]
        return CommandOutcome.succeeded();
    }
    // end::CommandsRun[]
}
// end::Commands[]

