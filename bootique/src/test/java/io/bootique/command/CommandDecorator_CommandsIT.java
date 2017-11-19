package io.bootique.command;

import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CommandDecorator_CommandsIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testAlsoRun_DecorateWithPrivate() {

        // use private "-s" command in decorator
        BQModuleProvider commandsOverride = Commands.builder().add(MainCommand.class).noModuleCommands().build();
        CommandDecorator decorator = CommandDecorator.alsoRun("s");

        BQRuntime runtime = createRuntime(commandsOverride, decorator);
        CommandOutcome outcome = runtime.run();

        waitForAllToFinish();

        assertTrue(outcome.isSuccess());
        assertTrue(getCommand(runtime, MainCommand.class).isExecuted());
        assertTrue(getCommand(runtime, SuccessfulCommand.class).isExecuted());
    }

    @Test
    public void testBeforeRun_DecorateWithPrivate() {

        // use private "-s" command in decorator
        BQModuleProvider commandsOverride = Commands.builder().add(MainCommand.class).noModuleCommands().build();
        CommandDecorator decorator = CommandDecorator.beforeRun("s");

        BQRuntime runtime = createRuntime(commandsOverride, decorator);
        CommandOutcome outcome = runtime.run();

        assertTrue(outcome.isSuccess());
        assertTrue(getCommand(runtime, MainCommand.class).isExecuted());
        assertTrue(getCommand(runtime, SuccessfulCommand.class).isExecuted());
    }

    private BQRuntime createRuntime(BQModuleProvider commandsOverride, CommandDecorator decorator) {
        return testFactory
                .app("--a")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(MainCommand.class)
                        .addCommand(SuccessfulCommand.class)
                        .decorateCommand(MainCommand.class, decorator))
                .module(commandsOverride)
                .createRuntime();
    }

    private <T extends Command> T getCommand(BQRuntime runtime, Class<T> type) {
        return (T) runtime.getInstance(CommandManager.class).lookupByType(type).getCommand();
    }

    private void waitForAllToFinish() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MainCommand extends CommandDecoratorIT.ExecutableOnceCommand {

        private static final String NAME = "a";

        MainCommand() {
            super(NAME);
        }
    }

    private static class SuccessfulCommand extends CommandDecoratorIT.ExecutableOnceCommand {

        private static final String NAME = "s";

        SuccessfulCommand() {
            super(NAME);
        }
    }
}
