package io.bootique.jopt;

import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.DefaultCommandManager;
import io.bootique.log.BootLogger;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoptCliProviderCommandNameTest {

    private BootLogger mockBootLogger;
    private Map<String, Command> commands;

    @Before
    public void before() {
        this.mockBootLogger = mock(BootLogger.class);
        this.commands = new HashMap<>();
    }

    @Test
    public void testCommandName_NoMatch() {

        addMockCommand("c1", "me", "them");

        Cli cli = createCli("--me");
        assertNull(cli.commandName());
    }

    @Test
    public void testCommandName_Match() {

        addMockCommand("c1", "me", "them");
        addMockCommand("c2", "us", "others");

        Cli cli = createCli("--me --c1");
        assertEquals("c1", cli.commandName());
    }

    @Test(expected = RuntimeException.class)
    public void testCommandName_MultipleMatches() {

        addMockCommand("c1", "me", "them");
        addMockCommand("c2", "us", "others");

        createCli("--me --c1 --c2");
    }

    private void addMockCommand(String name, String... options) {

        Command mock = mock(Command.class);

        // for now JopCLiProvider adds command name as an option;
        // using this option in command line would match the original command
        // name

        CommandMetadata.Builder builder = CommandMetadata.builder(name);
        Arrays.asList(options).forEach(opt -> builder.addOption(OptionMetadata.builder(opt)));

        CommandMetadata md = builder.build();

        when(mock.getMetadata()).thenReturn(md);
        commands.put(mock.getMetadata().getName(), mock);
    }

    private Cli createCli(String args) {
        String[] argsArray = args.split(" ");

        Optional<Command> mockDefaultCommand = Optional.of(mock(Command.class));
        Optional<Command> mockHelpCommand = Optional.of(mock(Command.class));
        CommandManager commandManager = new DefaultCommandManager(commands, mockDefaultCommand, mockHelpCommand);

        ApplicationMetadata.Builder appBuilder = ApplicationMetadata.builder();
        commands.values().forEach(c -> appBuilder.addCommand(c.getMetadata()));

        return new JoptCliProvider(mockBootLogger, () -> commandManager, appBuilder.build(), argsArray).get();
    }

}
