/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.command;

import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;
import io.bootique.log.BootLogger;
import jakarta.inject.Provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A composite command made of the main command and auxiliary commands run before the main command or in parallel with it.
 */
class MultiCommand extends CommandWithMetadata {

    private final Command mainCommand;
    private final CommandDecorator extraCommands;
    private final Provider<CliFactory> cliFactoryProvider;
    private final Provider<CommandManager> commandManagerProvider;
    private final Provider<ExecutorService> executorProvider;
    private BootLogger logger;

    public MultiCommand(
            Command mainCommand,
            CommandDecorator extraCommands,
            Provider<CliFactory> cliFactoryProvider,
            Provider<CommandManager> commandManagerProvider,
            Provider<ExecutorService> executorProvider,
            BootLogger logger) {

        super(mainCommand.getMetadata());

        this.logger = logger;
        this.mainCommand = mainCommand;
        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
        this.extraCommands = extraCommands;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        // run "before" commands
        Collection<CommandOutcome> beforeResults = runBlocking(extraCommands.getBefore());
        for (CommandOutcome outcome : beforeResults) {
            if (!outcome.isSuccess()) {
                // for now returning the first failure...
                // TODO: combine all failures into a single message?
                return outcome;
            }
        }

        // run "also" commands... pass the logger to log failures
        runNonBlocking(extraCommands.getParallel(), this::logOutcome);

        return mainCommand.run(cli);
    }

    private Collection<CommandOutcome> runBlocking(Collection<CommandRefWithArgs> cmdRefs) {

        switch (cmdRefs.size()) {
            case 0:
                return Collections.emptyList();
            case 1:
                return runBlockingSingle(cmdRefs.iterator().next());
            default:
                return runBlockingMultiple(cmdRefs);
        }
    }

    private Collection<CommandOutcome> runBlockingSingle(CommandRefWithArgs cmdRef) {
        // a single command - we can bypass the thread pool...
        return Collections.singletonList(run(cmdRef, this::noLogOutcome));
    }

    private Collection<CommandOutcome> runBlockingMultiple(Collection<CommandRefWithArgs> cmdRefs) {

        Collection<CommandOutcome> outcomes = new ArrayList<>(cmdRefs.size());

        runNonBlocking(cmdRefs, this::noLogOutcome).forEach(future -> {

            try {
                outcomes.add(future.get());
            } catch (InterruptedException e) {
                // when interrupted, throw error rather than return CommandOutcome#failed()
                // see comment in toOutcomeSupplier() method for details
                throw new BootiqueException(1, "Interrupted", e);
            } catch (ExecutionException e) {
                // we don't expect futures to ever throw errors
                throw new BootiqueException(1, "Unexpected error", e);
            }
        });

        return outcomes;
    }

    private Collection<Future<CommandOutcome>> runNonBlocking(
            Collection<CommandRefWithArgs> cmdRefs,
            Consumer<CommandOutcome> outcomeListener) {

        // exit early, avoid pool creation if we don't need it
        if (cmdRefs.isEmpty()) {
            return Collections.emptyList();
        }

        ExecutorService executor = getExecutor();

        List<Future<CommandOutcome>> futureOutcomes = new ArrayList<>(cmdRefs.size());
        cmdRefs.forEach(ref -> futureOutcomes.add(executor.submit(() -> run(ref, outcomeListener))));

        return futureOutcomes;
    }

    private CommandOutcome run(CommandRefWithArgs cmdRef, Consumer<CommandOutcome> outcomeListener) {

        CommandOutcome outcome;
        CommandManager commandManager = getCommandManager();

        // wrap both command resolving and execution in try/catch... Both can have errors...
        try {
            Cli cli = getCliFactory().createCli(cmdRef.getArgs());
            Command command = cmdRef.resolve(commandManager);
            outcome = command.run(cli);
        }
        // TODO: we need to distinguish between interrupts and other errors and re-throw interrupts
        // (and require commands to re-throw InterruptedException instead of wrapping it in a CommandOutcome#failed()),
        // because otherwise an interrupt will not be guaranteed to terminate the chain of commands
        //catch (InterruptedException e) {
        //       throw new BootiqueException(1, "Interrupted", e);
        // }
        catch (Exception e) {
            outcome = CommandOutcome.failed(1, e);
        }

        // log the real outcome
        outcomeListener.accept(outcome);

        // always return success, unless explicitly required to fail on errors
        return cmdRef.shouldTerminateOnErrors() ? outcome : CommandOutcome.succeeded();
    }

    private void noLogOutcome(CommandOutcome outcome) {
        // do nothing ..  this is an outcome logger when no logging should occur.
    }

    private void logOutcome(CommandOutcome outcome) {

        // TODO: track outcomes by command name to provide better failure diagnostics

        if (outcome.isSuccess()) {
            logger.trace(() -> "Command succeeded");
        } else {
            if (outcome.getMessage() != null) {
                logger.stderr(String.format("Error running command: %s", outcome.getMessage()), outcome.getException());
            } else {
                logger.stderr(String.format("Error running command"), outcome.getException());
            }
        }
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
