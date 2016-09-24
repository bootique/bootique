package io.bootique.jopt;

import io.bootique.application.CommandMetadata;
import io.bootique.application.OptionMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.DefaultCommandManager;
import io.bootique.log.BootLogger;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoptCliProviderTest {

    private BootLogger mockBootLogger;
    private Map<String, Command> commands;
    private Set<OptionMetadata> options;

    @Before
    public void before() {
        this.mockBootLogger = mock(BootLogger.class);
        this.commands = new HashMap<>();
        this.options = new HashSet<>();
    }

    @Test
    public void testGet_HasOption() {

        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me")));

        assertTrue(createCli("-m").hasOption("me"));
        assertTrue(createCli("--me").hasOption("me"));
        assertFalse(createCli("-m").hasOption("not_me"));
        assertFalse(createCli("-m").hasOption("m"));
    }

    @Test
    public void testOptionStrings_Short() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null)));

        assertEquals(createCli("-m v4").optionStrings("me"), "v4");
    }

    @Test
    public void testOptionStrings_Long_Equals() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null)));

        assertEquals(createCli("--me=v4").optionStrings("me"), "v4");
    }

    @Test
    public void testOptionStrings_Long_Space() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null)));

        assertEquals(createCli("--me v4").optionStrings("me"), "v4");
    }

    @Test
    public void testOptionStrings_Single_Mixed() {

        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null))
                .addOption(OptionMetadata.builder("other").valueOptional(null)));

        assertEquals(createCli("--other v2 --me=v4").optionStrings("me"), "v4");
    }

    @Test
    public void testOptionStrings_Multiple_Mixed() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null))
                .addOption(OptionMetadata.builder("other").valueOptional(null))
                .addOption(OptionMetadata.builder("n").valueOptional(null)).addOption(OptionMetadata.builder("yes")));

        assertEquals(createCli("--me=v1 --other v2 -n v3 --me v4 --yes").optionStrings("me"), "v1", "v4");
    }

    @Test
    public void testStandaloneArguments_Mix() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null))
                .addOption(OptionMetadata.builder("other").valueOptional(null)).addOption(OptionMetadata.builder("yes")));

        assertEquals(createCli("a --me=v1 --other v2 b --me v4 --yes c d").standaloneArguments(), "a", "b", "c", "d");
    }

    @Test
    public void testStandaloneArguments_DashDash() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null))
                .addOption(OptionMetadata.builder("other").valueOptional(null)));

        assertEquals(createCli("a --me=v1 -- --other v2").standaloneArguments(), "a", "--other", "v2");
    }

    @Test
    @Deprecated
    public void testPrintHelp_BareCommand() {

        addMockCommand(CommandMetadata.builder("c1"));

        StringWriter out = new StringWriter();
        createCli("").printHelp(out);

        String help = out.toString();
        assertTrue(help.indexOf("--c1") >= 0);
    }

    @Test
    @Deprecated
    public void testPrintHelp_CommandWithOptions() {

        addMockCommand(CommandMetadata.builder("c1").description("c1_description")
                .addOption(OptionMetadata.builder("o1").valueOptional("value_of_o1"))
                .addOption(OptionMetadata.builder("o2").valueOptional(null)));

        StringWriter out = new StringWriter();
        createCli("").printHelp(out);

        String help = out.toString();
        assertTrue(help.indexOf("--c1") >= 0);
        assertTrue(help.indexOf("c1_description") >= 0);
        assertTrue(help.indexOf("--o1") >= 0);
        assertTrue(help.indexOf("--o2") >= 0);
        assertTrue(help.indexOf("value_of_o1") >= 0);
    }

    private void assertEquals(Collection<String> result, String... expected) {
        assertArrayEquals(expected, result.toArray());
    }

    private Command addMockCommand(CommandMetadata.Builder metadataBuilder) {
        Command mock = mock(Command.class);
        when(mock.getMetadata()).thenReturn(metadataBuilder.build());
        commands.put(mock.getMetadata().getName(), mock);
        return mock;
    }

    private Cli createCli(String args) {
        String[] argsArray = args.split(" ");

        Optional<Command> mockDefaultCommand = Optional.of(mock(Command.class));
        Optional<Command> mockHelpCommand = Optional.of(mock(Command.class));
        CommandManager commandManager = new DefaultCommandManager(commands, mockDefaultCommand, mockHelpCommand);

        return new JoptCliProvider(mockBootLogger, commandManager, options, argsArray).get();
    }
}
