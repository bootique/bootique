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

        // 1. start some process in a different thread ...
        // executor.submit(() -> /* do something */ );

        // 2. Tell Bootique that the process is still running
        return CommandOutcome.succeededAndForkedToBackground();
    }
    // end::run[]
}

