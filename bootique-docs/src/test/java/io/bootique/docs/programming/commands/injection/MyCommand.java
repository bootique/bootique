package io.bootique.docs.programming.commands.injection;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class MyCommand implements Command {

    // tag::Commands[]
    @Inject
    private Provider<SomeService> provider;

    @Override
    public CommandOutcome run(Cli cli) {
        provider.get().someMethod();
        // end::Commands[]
        return null;
        // tag::Commands[]
    }
    // end::Commands[]
}

