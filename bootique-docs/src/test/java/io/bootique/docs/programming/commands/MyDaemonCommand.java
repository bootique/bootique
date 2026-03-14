package io.bootique.docs.programming.commands;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyDaemonCommand implements Command {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // tag::run[]
    @Override
    public CommandOutcome run(Cli cli) {

        // 1. start some process in a different thread ...
        executor.submit(() -> { /* do something */ });

        // 2. Tell Bootique that the process is still running
        return CommandOutcome.succeededAndForkedToBackground();
    }
    // end::run[]
}

