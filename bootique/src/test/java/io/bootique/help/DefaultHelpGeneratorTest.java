package io.bootique.help;

import io.bootique.cli.Cli;
import io.bootique.cli.CliOption;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandMetadata;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultHelpGeneratorTest {

    private CommandManager mockCommandManager;
    private Collection<Command> commands;
    private Collection<CliOption> standloneOptions;


    private static void assertLines(DefaultHelpGenerator generator, String... expectedLines) {

        StringBuilder expected = new StringBuilder();
        for (String s : expectedLines) {
            expected.append(s).append(DefaultHelpGenerator.NEWLINE);
        }

        String help = generator.generate();
        assertNotNull(help);
        assertEquals(expected.toString(), help);
    }

    @Before
    public void before() {

        this.commands = new ArrayList<>();
        this.standloneOptions = new ArrayList<>();

        this.mockCommandManager = mock(CommandManager.class);
        when(mockCommandManager.getCommands()).thenReturn(commands);
    }

    @Test
    public void testGenerate_Command_DefaultMetadata() {

        commands.add(new DefaultMetadataCommand());

        assertLines(new DefaultHelpGenerator(mockCommandManager, standloneOptions),
                "OPTIONS",
                "   -d, --defaultmetadata"
        );
    }

    static final class DefaultMetadataCommand extends CommandWithMetadata {

        public DefaultMetadataCommand() {
            super(CommandMetadata.builder(DefaultMetadataCommand.class));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

}
