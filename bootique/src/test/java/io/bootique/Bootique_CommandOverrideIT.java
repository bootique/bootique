package io.bootique;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
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
        testOverride_ParallelCommand_ByName_WithExtraArgs(false);
        assertTrue(successfulCommand.isExecuted());
    }

    @Test
    public void testOverride_ParallelCommand_ByName_WithArgs() {
        testOverride_ParallelCommand_ByName_WithExtraArgs(false, "--" + SuccessfulCommand.FLAG_OPTION);
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testOverride_ParallelCommand_ByType() {
        testOverride_ParallelCommand_ByName_WithExtraArgs(true);
        assertTrue(successfulCommand.isExecuted());
    }

    @Test
    public void testOverride_ParallelCommand_ByType_WithArgs() {
        testOverride_ParallelCommand_ByName_WithExtraArgs(true, "--" + SuccessfulCommand.FLAG_OPTION);
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    private void testOverride_ParallelCommand_ByName_WithExtraArgs(boolean byType, String... extraArgs) {
        String parallelCommandName = successfulCommand.getMetadata().getName();

        String[] args = new String[]{"-" + parallelCommandName};
        CommandDecorator decorator;

        if (byType) {
            decorator = CommandDecorator.builder().alsoRun(successfulCommand.getClass(), extraArgs).build();
        } else {
            args = concat(args, extraArgs);
            decorator = CommandDecorator.builder().alsoRun(args).build();
        }

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
        testOverride_FailureBeforeOriginal_ByName_WithExtraArgs(false);
        assertTrue(failingCommand.isExecuted());
    }

    @Test
    public void testOverride_FailureBeforeOriginal_ByName_WithArgs() {
        testOverride_FailureBeforeOriginal_ByName_WithExtraArgs(false, "--" + FailingCommand.FLAG_OPTION);
        assertTrue(failingCommand.isExecuted());
        assertTrue(failingCommand.hasFlagOption());
    }

    @Test
    public void testOverride_FailureBeforeOriginal_ByType() {
        testOverride_FailureBeforeOriginal_ByName_WithExtraArgs(true);
        assertTrue(failingCommand.isExecuted());
    }

    @Test
    public void testOverride_FailureBeforeOriginal_ByType_WithArgs() {
        testOverride_FailureBeforeOriginal_ByName_WithExtraArgs(true, "--" + FailingCommand.FLAG_OPTION);
        assertTrue(failingCommand.isExecuted());
        assertTrue(failingCommand.hasFlagOption());
    }

    private void testOverride_FailureBeforeOriginal_ByName_WithExtraArgs(boolean byType, String... extraArgs) {
        String failingCommandName = failingCommand.getMetadata().getName();

        String[] args = new String[]{"-" + failingCommandName};
        CommandDecorator decorator;

        if (byType) {
            decorator = CommandDecorator.builder().beforeRun(failingCommand.getClass(), extraArgs).build();
        } else {
            args = concat(args, extraArgs);
            decorator = CommandDecorator.builder().beforeRun(args).build();
        }

        testOverride_FailureBeforeOriginal(decorator);
        assertTrue(failingCommand.isExecuted());
    }

    private String[] concat(String[] arr1, String[] arr2) {
        if (arr2.length > 0) {
            String[] _args = new String[arr1.length + arr2.length];
            System.arraycopy(arr1, 0, _args, 0, arr1.length);
            System.arraycopy(arr2, 0, _args, arr1.length, arr2.length);
            arr1 = _args;
        }
        return arr1;
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
        public static final String FLAG_OPTION = "sflag";

        SuccessfulCommand(String commandName) {
            super(commandName, FLAG_OPTION, CommandOutcome.succeeded());
        }
    }

    private static class FailingCommand extends ExecutableOnceCommand {
        public static final String FLAG_OPTION = "fflag";

        FailingCommand(String commandName) {
            super(commandName, FLAG_OPTION, CommandOutcome.failed(1, commandName));
        }
    }

    private static class ExecutableOnceCommand extends CommandWithMetadata {

        private final Optional<String> flagOption;
        private final CommandOutcome outcome;
        private final AtomicBoolean executed;
        private final AtomicBoolean hasFlagOption;

        public ExecutableOnceCommand(String commandName, CommandOutcome outcome) {
            this(commandName, Optional.empty(), outcome);
        }

        public ExecutableOnceCommand(String commandName, String flagOption, CommandOutcome outcome) {
            this(commandName, Optional.of(flagOption), outcome);
        }

        private ExecutableOnceCommand(String commandName, Optional<String> flagOption, CommandOutcome outcome) {
            super(buildMetadata(commandName, flagOption));
            this.flagOption = flagOption;
            this.outcome = outcome;
            this.executed = new AtomicBoolean();
            this.hasFlagOption = new AtomicBoolean();
        }

        private static CommandMetadata buildMetadata(String commandName, Optional<String> flagOption) {
            CommandMetadata.Builder builder = CommandMetadata.builder(commandName);
            flagOption.ifPresent(o -> builder.addOption(OptionMetadata.builder(o)));
            return builder.build();
        }

        @Override
        public CommandOutcome run(Cli cli) {
            if (!executed.compareAndSet(false, true)) {
                throw new IllegalStateException("Already executed");
            }
            flagOption.ifPresent(o -> hasFlagOption.set(cli.hasOption(o)));
            return outcome;
        }

        public boolean isExecuted() {
            return executed.get();
        }

        public boolean hasFlagOption() {
            return hasFlagOption.get();
        }
    }
}
