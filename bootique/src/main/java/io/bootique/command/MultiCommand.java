package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A composite command made of the main command and auxiliary commands run before the main command or in parallel with it.
 *
 * @since 0.25
 */
class MultiCommand extends CommandWithMetadata {

    private final Command mainCommand;
    private final Provider<CliFactory> cliFactoryProvider;
    private final Provider<CommandManager> commandManagerProvider;
    private final Provider<ExecutorService> executorProvider;
    private final CommandDecorator extraCommands;

    public MultiCommand(Command mainCommand,
                        CommandDecorator extraCommands,
                        Provider<CliFactory> cliFactoryProvider,
                        Provider<CommandManager> commandManagerProvider,
                        Provider<ExecutorService> executorProvider) {

        super(mainCommand.getMetadata());
        this.mainCommand = mainCommand;
        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
        this.extraCommands = extraCommands;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        Collection<CommandOutcome> failures = run(extraCommands.getBefore()).stream().map(future -> {
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

        run(extraCommands.getParallel()); // not waiting for the outcome?

        return mainCommand.run(cli);
    }

    private Collection<CompletableFuture<CommandOutcome>> run(Collection<CommandWithArgs> invocations) {
        ExecutorService executor = getExecutor();

        return invocations.stream()
                .map(this::toOutcomeSupplier)
                .map(outcomeSupplier -> CompletableFuture.supplyAsync(outcomeSupplier, executor))
                .collect(Collectors.toList());
    }

    private Supplier<CommandOutcome> toOutcomeSupplier(CommandWithArgs invocation) {

        CommandManager commandManager = getCommandManager();
        String commandName = invocation.getName(commandManager);
        Cli cli = getCliFactory().createCli(commandName, invocation.getArgs());
        Command command = commandManager.lookupByName(commandName);

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
