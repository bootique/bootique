package io.bootique.command;

import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommandsIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private String[] args;

    private static void assertCommandKeys(Map<String, ManagedCommand> commands, String... expectedCommands) {
        assertEquals(commands.size(), expectedCommands.length);
        assertEquals(new HashSet<>(asList(expectedCommands)), commands.keySet());
    }

    @Before
    public void before() {
        args = new String[]{"a", "b", "c"};
    }

    @Test
    public void testModuleCommands() {
        BQModuleProvider provider = Commands.builder().build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");

        assertFalse(commands.get("help").isHidden());
        assertFalse(commands.get("help").isDefault());
        assertTrue(commands.get("help").isHelp());

        assertFalse(commands.get("help-config").isHidden());
        assertFalse(commands.get("help-config").isDefault());
    }

    @Test
    public void testNoModuleCommands() {
        BQModuleProvider provider = Commands.builder().noModuleCommands().build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");

        assertTrue(commands.get("help").isHidden());
        assertFalse(commands.get("help").isDefault());
        assertTrue(commands.get("help").isHelp());

        assertTrue(commands.get("help-config").isHidden());
        assertFalse(commands.get("help-config").isDefault());
    }

    @Test
    public void testModule_ExtraCommandAsType() {
        BQModuleProvider provider = Commands.builder(C1.class).build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "c1", "help", "help-config");

        assertFalse(commands.get("help").isHidden());
        assertFalse(commands.get("help").isDefault());
        assertTrue(commands.get("help").isHelp());

        assertFalse(commands.get("help-config").isHidden());
        assertFalse(commands.get("help-config").isDefault());

        assertTrue(commands.containsKey("c1"));
        assertFalse(commands.get("c1").isDefault());
    }

    @Test
    public void testModule_ExtraCommandAsInstance() {
        BQModuleProvider provider = Commands.builder().add(new C1()).build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "c1", "help", "help-config");
    }

    @Test
    public void testModule_ExtraCommandOverride() {
        BQModuleProvider provider = Commands.builder().add(C2_Help.class).build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");
    }

    static class C1 implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class C2_Help implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }

        @Override
        public CommandMetadata getMetadata() {
            return CommandMetadata.builder("help").build();
        }
    }
}
