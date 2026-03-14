package io.bootique.docs.programming.commands.custom;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;

// tag::command[]
public class MyCommand extends CommandWithMetadata {

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(MyCommand.class)
                .description("My command does something important.")
                .build();
    }

    public MyCommand() {
        super(createMetadata());
    }

    @Override
    public CommandOutcome run(Cli cli) {

        // ... run the command here....

        return CommandOutcome.succeeded();
    }

}
// end::command[]

