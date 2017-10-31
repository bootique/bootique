package io.bootique;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Bootique_CommandOverrideIT {

    private static final String DEFAULT_COMMAND = "a";

    private ExecutableOnceCommand originalCommand;
    private Bootique bootique;

    @Before
    public void before() {
        originalCommand = new ExecutableOnceCommand(DEFAULT_COMMAND, CommandOutcome.succeeded());
        bootique = Bootique.app("-" + DEFAULT_COMMAND).module(b -> BQCoreModule.extend(b).addCommand(originalCommand));
    }

    @Test
    public void testOverride_ParallelCommand() {
        String executableCommandName = "b";
        ExecutableOnceCommand executableCommand = successfulCommand(executableCommandName);

        bootique.module(binder -> BQCoreModule.extend(binder)
                .addCommand(executableCommand));
        bootique.module(binder -> BQCoreModule.extend(binder)
                .addCommandDecorator(DEFAULT_COMMAND, CommandDecorator.builder().alsoRun("-" + executableCommandName)));

        CommandOutcome outcome = bootique.exec();
        assertTrue(outcome.isSuccess());
        assertTrue(originalCommand.isExecuted());
        assertTrue(executableCommand.isExecuted());
    }

    @Test
    public void testOverride_FailureBeforeOriginal() {
        String failingCommandName = "b";
        ExecutableOnceCommand failingCommand = failingCommand(failingCommandName);

        bootique.module(binder -> BQCoreModule.extend(binder)
                .addCommand(failingCommand));
        bootique.module(binder -> BQCoreModule.extend(binder)
                .addCommandDecorator(DEFAULT_COMMAND, CommandDecorator.builder().beforeRun("-" + failingCommandName)));

        CommandOutcome outcome = bootique.exec();
        assertFalse(outcome.isSuccess());
        assertNull(outcome.getException());
        assertFalse(originalCommand.isExecuted());
        assertTrue(failingCommand.isExecuted());
        // TODO: modify the check after io.bootique.command.OverridenCommand#run() is updated
        assertEquals("Some of the commands failed", outcome.getMessage());
    }

    private static ExecutableOnceCommand successfulCommand(String commandName) {
        return new ExecutableOnceCommand(commandName, CommandOutcome.succeeded());
    }

    private static ExecutableOnceCommand failingCommand(String commandName) {
        CommandOutcome outcome = CommandOutcome.failed(1, commandName);
        return new ExecutableOnceCommand(commandName, outcome);
    }

    private static class ExecutableOnceCommand implements Command {
        private final String commandName;
        private final CommandOutcome outcome;
        private final AtomicBoolean executed;

        ExecutableOnceCommand(String commandName, CommandOutcome outcome) {
            this.commandName = commandName;
            this.outcome = outcome;
            this.executed = new AtomicBoolean();
        }

        @Override
        public CommandOutcome run(Cli cli) {
            if (!executed.compareAndSet(false, true)) {
                throw new IllegalStateException("Already executed");
            }
            return outcome;
        }

        @Override
        public CommandMetadata getMetadata() {
            return CommandMetadata.builder(commandName).build();
        }

        public boolean isExecuted() {
            return executed.get();
        }
    }
}
