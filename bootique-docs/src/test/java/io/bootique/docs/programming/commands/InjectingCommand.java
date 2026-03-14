package io.bootique.docs.programming.commands;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class InjectingCommand implements Command {

    // tag::inject[]
    @Inject
    private Provider<SomeService> provider;

    @Override
    public CommandOutcome run(Cli cli) {
        provider.get().someMethod();
        return CommandOutcome.succeeded();
    }
    // end::inject[]

    static class SomeService {
        public void someMethod() {
        }
    }
}

