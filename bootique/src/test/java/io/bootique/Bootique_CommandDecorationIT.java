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
import java.util.concurrent.atomic.AtomicReference;

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
        this.mainCommand = new ExecutableOnceCommand("a");
        this.successfulCommand = new SuccessfulCommand();
        this.failingCommand = new FailingCommand();
    }

    private CommandOutcome decorateAndRun(CommandDecorator decorator) {
        return testFactory
                .app("--a")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(mainCommand)
                        .addCommand(successfulCommand)
                        .addCommand(failingCommand)
                        .decorateCommand(mainCommand.getClass(), decorator))
                .createRuntime()
                .run();
    }

    private CommandOutcome decorateRunAndWait(CommandDecorator decorator) {

        CommandOutcome outcome = decorateAndRun(decorator);

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

        CommandDecorator decorator = CommandDecorator.alsoRun("--s");

        assertTrue(decorateRunAndWait(decorator).isSuccess());
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_ByName_WithArgs() {

        CommandDecorator decorator = CommandDecorator.alsoRun("--s", "--sflag");

        assertTrue(decorateRunAndWait(decorator).isSuccess());
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_ByType() {

        CommandDecorator decorator = CommandDecorator.alsoRun(SuccessfulCommand.class);

        assertTrue(decorateRunAndWait(decorator).isSuccess());
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_ByType_WithArgs() {

        CommandDecorator decorator = CommandDecorator.alsoRun(SuccessfulCommand.class, "--sflag");

        decorateRunAndWait(decorator);
        assertTrue(mainCommand.isExecuted());
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testBeforeRun_Failure_ByName() {

        CommandDecorator decorator = CommandDecorator.beforeRun("--f");

        CommandOutcome outcome = decorateAndRun(decorator);

        failingCommand.assertFailure(outcome);
        assertFalse(mainCommand.isExecuted());
    }

    @Test
    public void testBeforeRun_Failure_ByName_WithArgs() {

        CommandDecorator decorator = CommandDecorator.beforeRun("--f", "--fflag");

        CommandOutcome outcome = decorateAndRun(decorator);

        failingCommand.assertFailure(outcome);
        assertTrue(failingCommand.hasFlagOption());
        assertFalse(mainCommand.isExecuted());
    }

    @Test
    public void testBeforeRun_Failure_ByType() {
        CommandDecorator decorator = CommandDecorator.beforeRun(FailingCommand.class);

        CommandOutcome outcome = decorateAndRun(decorator);

        failingCommand.assertFailure(outcome);
        assertFalse(mainCommand.isExecuted());
    }

    @Test
    public void testBeforeRun_Failure_ByType_WithArgs() {
        CommandDecorator decorator = CommandDecorator.beforeRun(FailingCommand.class, "--fflag");

        CommandOutcome outcome = decorateAndRun(decorator);

        failingCommand.assertFailure(outcome);
        assertTrue(failingCommand.hasFlagOption());
        assertFalse(mainCommand.isExecuted());
    }

    private static class SuccessfulCommand extends ExecutableOnceCommand {

        private static final String NAME = "s";
        private static final String FLAG_OPT = "sflag";

        SuccessfulCommand() {
            super(NAME, FLAG_OPT);
        }

        public boolean hasFlagOption() {
            return cliRef.get().hasOption(FLAG_OPT);
        }
    }

    private static class FailingCommand extends ExecutableOnceCommand {

        private static final String NAME = "f";
        private static final String FLAG_OPT = "fflag";

        FailingCommand() {
            super(NAME, FLAG_OPT);
        }

        void assertFailure(CommandOutcome outcome) {
            assertTrue(isExecuted());
            assertFalse(outcome.isSuccess());
            assertNull(outcome.getException());
            assertEquals("Some of the commands failed", outcome.getMessage());
        }

        public boolean hasFlagOption() {
            return cliRef.get().hasOption(FLAG_OPT);
        }

        @Override
        public CommandOutcome run(Cli cli) {
            super.run(cli);
            return CommandOutcome.failed(1, NAME);
        }
    }

    private static class ExecutableOnceCommand extends CommandWithMetadata {

        protected final AtomicReference<Cli> cliRef;

        public ExecutableOnceCommand(String commandName) {
            this(commandName, Optional.empty());
        }

        public ExecutableOnceCommand(String commandName, String flagOption) {
            this(commandName, Optional.of(flagOption));
        }

        private ExecutableOnceCommand(String commandName, Optional<String> flagOption) {
            super(buildMetadata(commandName, flagOption));
            this.cliRef = new AtomicReference<>();
        }

        private static CommandMetadata buildMetadata(String commandName, Optional<String> flagOption) {
            CommandMetadata.Builder builder = CommandMetadata.builder(commandName);
            flagOption.ifPresent(o -> builder.addOption(OptionMetadata.builder(o)));
            return builder.build();
        }

        @Override
        public CommandOutcome run(Cli cli) {
            if (!cliRef.compareAndSet(null, cli)) {
                throw new IllegalStateException("Already executed");
            }

            return CommandOutcome.succeeded();
        }

        public boolean isExecuted() {
            return cliRef.get() != null;
        }
    }
}
