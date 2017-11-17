package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DecoratedCommand extends CommandWithMetadata {

    private final Command originalCommand;
    private final Map<Class<? extends Command>, Command> commands;
    private final Provider<CliFactory> cliFactoryProvider;
    private final Provider<CommandManager> commandManagerProvider;
    private final Provider<ExecutorService> executorProvider;
    private final Collection<CommandInvocation> before;
    private final Collection<CommandInvocation> parallel;

    public DecoratedCommand(Command originalCommand,
                            Map<Class<? extends Command>, Command> commands,
                            Provider<CliFactory> cliFactoryProvider,
                            Provider<CommandManager> commandManagerProvider,
                            Provider<ExecutorService> executorProvider,
                            Collection<CommandInvocation> before,
                            Collection<CommandInvocation> parallel) {
        super(originalCommand.getMetadata());
        this.originalCommand = originalCommand;
        this.commands = commands;
        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
        this.before = before;
        this.parallel = parallel;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        Collection<CommandOutcome> failures = run(before).stream().map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        // when interrupted, throw error rather than return CommandOutcome#failed()
                        // see comment in toOutcomeSupplier() method for details
                        throw new BootiqueException(1, "Interrupted", e);
                    } catch (ExecutionException e) {
                        // we don't expect futures to ever throw errors
                        throw new BootiqueException(1, "Unexpected error", e);
                    }
                })
                .filter(outcome -> !outcome.isSuccess()).collect(Collectors.toList());

        if (failures.size() > 0) {
            // TODO: combine all results into a single message? or need a different type of CommandOutcome (e.g. MultiCommandOutcome)?
            return CommandOutcome.failed(1, "Some of the commands failed");
        }

        run(parallel); // not waiting for the outcome?

        return originalCommand.run(cli);
    }

    private Collection<CompletableFuture<CommandOutcome>> run(Collection<CommandInvocation> invocations) {
        ExecutorService executor = getExecutor();

        return invocations.stream()
                .map(this::toOutcomeSupplier)
                .map(outcomeSupplier -> CompletableFuture.supplyAsync(outcomeSupplier, executor))
                .collect(Collectors.toList());
    }

    private Supplier<CommandOutcome> toOutcomeSupplier(CommandInvocation invocation) {
        Optional<Command> commandOptional = invocation.getCommandType()
                .map(commandType -> Objects.requireNonNull(commands.get(commandType), "Unknown command type: " + commandType.getName()));

        Cli cli;
        if (commandOptional.isPresent()) {
            cli = getCliFactory().createCli(commandOptional.get(), invocation.getArgs());
        } else {
            cli = getCliFactory().createCli(invocation.getArgs());
        }

        String commandName = cli.commandName();
        Command command = getCommandManager().getCommands().get(commandName);
        if (command == null) {
            throw new BootiqueException(1, "Unknown command: " + commandName);
        }

        return () -> {
            CommandOutcome outcome;

            // TODO: we need to distinguish between interrupts and other errors and re-throw interrupts
            // (and require commands to re-throw InterruptedException instead of wrapping it in a CommandOutcome#failed()),
            // because otherwise an interrupt will not be guaranteed to terminate the chain of commands
            try {
                outcome = command.run(cli);
            } /*catch (InterruptedException e) {
                throw new BootiqueException(1, "Interrupted", e);
            }*/ catch (Exception e) {
                outcome = CommandOutcome.failed(1, e);
            }

            // always return success, unless explicitly required to fail on errors
            return invocation.shouldTerminateOnErrors() ? outcome : CommandOutcome.succeeded();
        };
    }

    private CliFactory getCliFactory() {
        return cliFactoryProvider.get();
    }

    private CommandManager getCommandManager() {
        return commandManagerProvider.get();
    }

    private ExecutorService getExecutor() {
        return executorProvider.get();
    }
}
