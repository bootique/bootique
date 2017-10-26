package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.cli.Cli;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

public class OverridenCommand extends CommandWithMetadata {

    private final Command originalCommand;
    private final Provider<ExecutorService> executorProvider;
    private final Collection<CommandInvocation> before;
    private final Collection<CommandInvocation> parallel;

    public OverridenCommand(Command originalCommand,
                            Provider<ExecutorService> executorProvider,
                            Collection<CommandInvocation> before,
                            Collection<CommandInvocation> parallel) {
        super(originalCommand.getMetadata());
        this.originalCommand = originalCommand;
        this.executorProvider = executorProvider;
        this.before = before;
        this.parallel = parallel;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        return null;
    }
}
