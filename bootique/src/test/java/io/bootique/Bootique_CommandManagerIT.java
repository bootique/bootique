package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandOutcome;
import io.bootique.help.HelpCommand;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Bootique_CommandManagerIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testHelpAndModuleCommands() {
        BQRuntime runtime = runtimeFactory.app().modules(M0.class, M1.class).createRuntime();

        CommandManager commandManager = runtime.getInstance(CommandManager.class);
        assertEquals("help, helpconfig and module commands must be present", 4, commandManager.getAllCommands().size());

        assertSame(M0.mockCommand, commandManager.lookupByName("m0command").getCommand());
        assertSame(M1.mockCommand, commandManager.lookupByName("m1command").getCommand());

        assertFalse(commandManager.getDefaultCommand().isPresent());
        assertSame(runtime.getInstance(HelpCommand.class), commandManager.getHelpCommand().get());
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
        assertSame(defaultCommand, commandManager.getDefaultCommand().get());
        assertSame(runtime.getInstance(HelpCommand.class), commandManager.getHelpCommand().get());
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
        assertSame(defaultCommand, commandManager.getDefaultCommand().get());
    }


    static class M0 implements Module {

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

    static class M1 implements Module {

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
