package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A composite command made of the main command and auxiliary commands run before the main command or in parallel with it.
 *
 * @since 0.25
 */
class MultiCommand extends CommandWithMetadata {

    private final Command mainCommand;
    private final CommandDecorator extraCommands;
    private final Provider<CliFactory> cliFactoryProvider;
    private final Provider<CommandManager> commandManagerProvider;
    private final Provider<ExecutorService> executorProvider;

    public MultiCommand(
            Command mainCommand,
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

        Collection<CommandOutcome> beforeFailures = runBlockingAndGetFailures(extraCommands.getBefore());

        if (beforeFailures.size() > 0) {
            // TODO: combine all results into a single message? or need a different type of CommandOutcome (e.g. MultiCommandOutcome)?
            return CommandOutcome.failed(1, "Some of the commands failed");
        }

        // not waiting for the outcome at least until we start supporting background non-terminating commands
        runNonBlocking(extraCommands.getParallel());

        return mainCommand.run(cli);
    }

    private Collection<CommandOutcome> runBlockingAndGetFailures(Collection<CommandRefWithArgs> cmdRefs) {

        switch (cmdRefs.size()) {
            case 0:
                return Collections.emptyList();
            case 1:
                return doRunBlockingAndGetFailures(cmdRefs.iterator().next());
            default:
                return doRunBlockingAndGetFailures(cmdRefs);
        }
    }

    private Collection<CommandOutcome> doRunBlockingAndGetFailures(CommandRefWithArgs cmdRef) {
        // a single command - we can bypass the thread pool...
        CommandOutcome outcome = run(cmdRef);
        return outcome.isSuccess() ? Collections.emptyList() : Collections.singletonList(outcome);
    }

    private Collection<CommandOutcome> doRunBlockingAndGetFailures(Collection<CommandRefWithArgs> cmdRefs) {

        Collection<CommandOutcome> failures = new ArrayList<>(3);

        runNonBlocking(cmdRefs).forEach(future -> {
            CommandOutcome outcome;

            try {
                outcome = future.get();
            } catch (InterruptedException e) {
                // when interrupted, throw error rather than return CommandOutcome#failed()
                // see comment in toOutcomeSupplier() method for details
                throw new BootiqueException(1, "Interrupted", e);
            } catch (ExecutionException e) {
                // we don't expect futures to ever throw errors
                throw new BootiqueException(1, "Unexpected error", e);
            }

            if (!outcome.isSuccess()) {
                failures.add(outcome);
            }
        });

        return failures;
    }

    private Collection<Future<CommandOutcome>> runNonBlocking(Collection<CommandRefWithArgs> cmdRefs) {

        // exit early, avoid pool creation if we don't need it
        if(cmdRefs.isEmpty()) {
            return Collections.emptyList();
        }

        ExecutorService executor = getExecutor();

        List<Future<CommandOutcome>> futureOutcomes = new ArrayList<>(cmdRefs.size());
        cmdRefs.forEach(ref -> futureOutcomes.add(executor.submit(() -> run(ref))));

        return futureOutcomes;
    }

    private CommandOutcome run(CommandRefWithArgs cmdRef) {

        CommandManager commandManager = getCommandManager();
        Cli cli = getCliFactory().createCli(cmdRef.getArgs());
        Command command = cmdRef.resolve(commandManager);

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
        return cmdRef.shouldTerminateOnErrors() ? outcome : CommandOutcome.succeeded();
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
