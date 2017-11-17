package io.bootique;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Bootique_CommandDecorationIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private ExecutableOnceCommand mainCommand;
    private SuccessfulCommand successfulCommand;
    private FailingCommand failingCommand;

    @Before
    public void before() {
        this.mainCommand = new ExecutableOnceCommand("a", CommandOutcome.succeeded());
        this.successfulCommand = new SuccessfulCommand("s");
        this.failingCommand = new FailingCommand("f");
    }

    private CommandOutcome runWithDecorator(CommandDecorator decorator) {
        return testFactory
                .app("--a")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(mainCommand)
                        .addCommand(successfulCommand)
                        .addCommand(failingCommand)
                        .addCommandDecorator(mainCommand.getClass(), decorator))
                .createRuntime()
                .run();
    }

    private CommandOutcome runWithParallelDecorator(CommandDecorator decorator) {

        CommandOutcome outcome = runWithDecorator(decorator);

        // wait for the parallel commands to finish
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return outcome;
    }

    @Test
    public void testAlsoRun_ByName() {

        CommandDecorator decorator = CommandDecorator
                .builder()
                .alsoRun("--s")
                .build();

        assertTrue(runWithParallelDecorator(decorator).isSuccess());
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_ByName_WithArgs() {

        CommandDecorator decorator = CommandDecorator
                .builder()
                .alsoRun("--s", "--sflag")
                .build();

        assertTrue(runWithParallelDecorator(decorator).isSuccess());
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_ByType() {

        CommandDecorator decorator = CommandDecorator
                .builder()
                .alsoRun(SuccessfulCommand.class)
                .build();

        assertTrue(runWithParallelDecorator(decorator).isSuccess());
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_ByType_WithArgs() {

        CommandDecorator decorator = CommandDecorator
                .builder()
                .alsoRun(SuccessfulCommand.class, "--sflag")
                .build();

        runWithParallelDecorator(decorator);
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testRunBefore_Failure_ByName() {

        CommandDecorator decorator = CommandDecorator
                .builder()
                .beforeRun("--f")
                .build();

        CommandOutcome outcome = runWithDecorator(decorator);

        failingCommand.assertFailure(outcome);
        assertFalse(mainCommand.isExecuted());
    }

    @Test
    public void testRunBefore_Failure_ByName_WithArgs() {

        CommandDecorator decorator = CommandDecorator
                .builder()
                .beforeRun("--f", "--fflag")
                .build();

        CommandOutcome outcome = runWithDecorator(decorator);

        failingCommand.assertFailure(outcome);
        assertTrue(failingCommand.hasFlagOption());
        assertFalse(mainCommand.isExecuted());
    }

    @Test
    public void testRunBefore_Failure_ByType() {
        CommandDecorator decorator = CommandDecorator
                .builder()
                .beforeRun(FailingCommand.class)
                .build();

        CommandOutcome outcome = runWithDecorator(decorator);

        failingCommand.assertFailure(outcome);
        assertFalse(mainCommand.isExecuted());
    }

    @Test
    public void testRunBefore_Failure_ByType_WithArgs() {
        CommandDecorator decorator = CommandDecorator
                .builder()
                .beforeRun(FailingCommand.class, "--fflag")
                .build();

        CommandOutcome outcome = runWithDecorator(decorator);

        failingCommand.assertFailure(outcome);
        assertTrue(failingCommand.hasFlagOption());
        assertFalse(mainCommand.isExecuted());
    }

    private static class SuccessfulCommand extends ExecutableOnceCommand {
        SuccessfulCommand(String commandName) {
            super(commandName, "sflag", CommandOutcome.succeeded());
        }
    }

    private static class FailingCommand extends ExecutableOnceCommand {

        FailingCommand(String commandName) {
            super(commandName, "fflag", CommandOutcome.failed(1, commandName));
        }

        void assertFailure(CommandOutcome outcome) {
            assertTrue(isExecuted());
            assertFalse(outcome.isSuccess());
            assertNull(outcome.getException());
            assertEquals("Some of the commands failed", outcome.getMessage());
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
