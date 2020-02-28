package io.bootique.docs.programming.commands.injection;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;

import javax.inject.Inject;
import javax.inject.Provider;

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

