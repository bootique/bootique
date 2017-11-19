package io.bootique.jopt;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.DefaultCommandManager;
import io.bootique.command.ManagedCommand;
import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoptCliFactoryTest {

    private Map<String, ManagedCommand> commands;

    @Before
    public void before() {
        this.commands = new HashMap<>();
    }

    @Test
    public void testGet_HasOption() {

        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me")));

        assertTrue(createCli("-m").hasOption("me"));
        assertTrue(createCli("--me").hasOption("me"));
        assertFalse(createCli("-m").hasOption("not_me"));
        assertTrue(createCli("-m").hasOption("m"));
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

    private void assertEquals(Collection<String> result, String... expected) {
        assertArrayEquals(expected, result.toArray());
    }

    private Command addMockCommand(CommandMetadata.Builder metadataBuilder) {
        Command mock = mock(Command.class);
        when(mock.getMetadata()).thenReturn(metadataBuilder.build());
        commands.put(mock.getMetadata().getName(), ManagedCommand.builder(mock).build());
        return mock;
    }

    private Cli createCli(String args) {
        String[] argsArray = args.split(" ");

        CommandManager commandManager = new DefaultCommandManager(commands);

        ApplicationMetadata.Builder appBuilder = ApplicationMetadata.builder();
        commands.values().forEach(mc -> appBuilder.addCommand(mc.getCommand().getMetadata()));

        return new JoptCliFactory(() -> commandManager, appBuilder.build()).createCli(argsArray);
    }
}
