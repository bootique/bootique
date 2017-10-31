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

    private ExecutableOnceCommand originalCommand;
    private SuccessfulCommand successfulCommand;
    private FailingCommand failingCommand;
    private Bootique bootique;

    @Before
    public void before() {
        String DEFAULT_COMMAND = "a";

        originalCommand = new ExecutableOnceCommand(DEFAULT_COMMAND, CommandOutcome.succeeded());
        successfulCommand = successfulCommand("s");
        failingCommand = failingCommand("f");
        bootique = Bootique.app("-" + DEFAULT_COMMAND)
                .module(b -> {
                    BQCoreModule.extend(b).addCommand(originalCommand);
                    BQCoreModule.extend(b).addCommand(successfulCommand);
                    BQCoreModule.extend(b).addCommand(failingCommand);
                });
    }

    @Test
    public void testOverride_ParallelCommand_ByName() {
        String parallelCommandName = successfulCommand.getMetadata().getName();
        CommandDecorator decorator = CommandDecorator.builder().alsoRun(new String[]{"-" + parallelCommandName}).build();
        testOverride_ParallelCommand(decorator);
        assertTrue(successfulCommand.isExecuted());
    }

    @Test
    public void testOverride_ParallelCommand_ByType() {
        CommandDecorator decorator = CommandDecorator.builder().alsoRun(successfulCommand.getClass()).build();
        testOverride_ParallelCommand(decorator);
        assertTrue(successfulCommand.isExecuted());
    }

    private void testOverride_ParallelCommand(CommandDecorator decorator) {
        bootique.module(binder -> BQCoreModule.extend(binder)
                .addCommandDecorator(originalCommand.getClass(), decorator));

        CommandOutcome outcome = bootique.exec();
        assertTrue(outcome.isSuccess());
        assertTrue(originalCommand.isExecuted());
    }

    @Test
    public void testOverride_FailureBeforeOriginal_ByName() {
        String failingCommandName = failingCommand.getMetadata().getName();
        CommandDecorator decorator = CommandDecorator.builder().beforeRun(new String[]{"-" + failingCommandName}).build();
        testOverride_FailureBeforeOriginal(decorator);
        assertTrue(failingCommand.isExecuted());
    }

    @Test
    public void testOverride_FailureBeforeOriginal_ByType() {
        CommandDecorator decorator = CommandDecorator.builder().beforeRun(failingCommand.getClass()).build();
        testOverride_FailureBeforeOriginal(decorator);
        assertTrue(failingCommand.isExecuted());
    }

    private void testOverride_FailureBeforeOriginal(CommandDecorator decorator) {
        bootique.module(binder -> BQCoreModule.extend(binder)
                .addCommandDecorator(originalCommand.getClass(), decorator));

        CommandOutcome outcome = bootique.exec();
        assertFalse(outcome.isSuccess());
        assertNull(outcome.getException());
        assertFalse(originalCommand.isExecuted());
        // TODO: modify the check after io.bootique.command.OverridenCommand#run() is updated
        assertEquals("Some of the commands failed", outcome.getMessage());
    }

    private static SuccessfulCommand successfulCommand(String commandName) {
        return new SuccessfulCommand(commandName);
    }

    private static FailingCommand failingCommand(String commandName) {
        return new FailingCommand(commandName);
    }

    private static class SuccessfulCommand extends ExecutableOnceCommand {
        SuccessfulCommand(String commandName) {
            super(commandName, CommandOutcome.succeeded());
        }
    }

    private static class FailingCommand extends ExecutableOnceCommand {
        FailingCommand(String commandName) {
            super(commandName, CommandOutcome.failed(1, commandName));
        }
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
