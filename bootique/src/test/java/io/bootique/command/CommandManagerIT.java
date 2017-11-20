package io.bootique.command;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.help.HelpCommand;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandManagerIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testHelpAndModuleCommands() {
        BQRuntime runtime = runtimeFactory.app().modules(M0.class, M1.class).createRuntime();

        CommandManager commandManager = runtime.getInstance(CommandManager.class);
        assertEquals("help, helpconfig and module commands must be present", 4, commandManager.getAllCommands().size());

        assertSame(M0.mockCommand, commandManager.lookupByName("m0command").getCommand());
        assertSame(M1.mockCommand, commandManager.lookupByName("m1command").getCommand());

        assertFalse(commandManager.getPublicDefaultCommand().isPresent());
        assertSame(runtime.getInstance(HelpCommand.class), commandManager.getPublicHelpCommand().get());
    }

    @Test
    public void testDefaultAndHelpAndModuleCommands() {

        Command defaultCommand = cli -> CommandOutcome.succeeded();
        Module defaultCommandModule = binder -> BQCoreModule.extend(binder).setDefaultCommand(defaultCommand);

        BQRuntime runtime = runtimeFactory.app().modules(M0.class, M1.class).module(defaultCommandModule).createRuntime();

        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        assertEquals(5, commandManager.getAllCommands().size());
        assertSame(M0.mockCommand, commandManager.lookupByName("m0command").getCommand());
        assertSame(M1.mockCommand, commandManager.lookupByName("m1command").getCommand());
        assertSame(defaultCommand, commandManager.getPublicDefaultCommand().get());
        assertSame(runtime.getInstance(HelpCommand.class), commandManager.getPublicHelpCommand().get());
    }

    @Test
    public void testHiddenCommands() {

        Command hiddenCommand = new Command() {
            @Override
            public CommandOutcome run(Cli cli) {
                return CommandOutcome.succeeded();
            }

            @Override
            public CommandMetadata getMetadata() {
                return CommandMetadata.builder("xyz").hidden().build();
            }
        };

        BQRuntime runtime = runtimeFactory.app()
                .module(binder -> BQCoreModule.extend(binder).addCommand(hiddenCommand))
                .createRuntime();

        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        assertEquals(3, commandManager.getAllCommands().size());

        ManagedCommand hiddenManaged = commandManager.getAllCommands().get("xyz");
        assertSame(hiddenCommand, hiddenManaged.getCommand());
        assertTrue(hiddenManaged.isHidden());
    }

    @Test
    public void testDefaultCommandWithMetadata() {

        Command defaultCommand = new Command() {
            @Override
            public CommandOutcome run(Cli cli) {
                return CommandOutcome.succeeded();
            }

            @Override
            public CommandMetadata getMetadata() {
                // note how this name intentionally matches one of the existing commands
                return CommandMetadata.builder("m0command").build();
            }
        };

        Module defaultCommandModule = binder -> BQCoreModule.extend(binder).setDefaultCommand(defaultCommand);

        BQRuntime runtime = runtimeFactory.app()
                .modules(M0.class, M1.class)
                .module(defaultCommandModule)
                .createRuntime();

        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        // the main assertion we care about...
        assertSame("Default command did not override another command with same name",
                defaultCommand,
                commandManager.lookupByName("m0command").getCommand());

        // sanity check
        assertEquals(4, commandManager.getAllCommands().size());
        assertSame(M1.mockCommand, commandManager.lookupByName("m1command").getCommand());
        assertSame(runtime.getInstance(HelpCommand.class), commandManager.lookupByName("help").getCommand());
        assertSame(defaultCommand, commandManager.getPublicDefaultCommand().get());
    }


    public static class M0 implements Module {

        static final Command mockCommand;

        static {
            mockCommand = mock(Command.class);
            when(mockCommand.getMetadata()).thenReturn(CommandMetadata.builder("m0command").build());
        }

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(mockCommand);
        }
    }

    public static class M1 implements Module {

        static final Command mockCommand;

        static {
            mockCommand = mock(Command.class);
            when(mockCommand.getMetadata()).thenReturn(CommandMetadata.builder("m1command").build());
        }

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(mockCommand);
        }
    }
}
