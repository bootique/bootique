package io.bootique.run;

import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandOutcome;
import io.bootique.command.DefaultCommandManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultRunnerTest {

    private Cli mockCli;

    private static Command mockCommand(String name, CommandOutcome outcome, String... options) {

        CommandMetadata.Builder builder = CommandMetadata.builder(name);
        Arrays.asList(options).forEach(opt -> builder.addOption(OptionMetadata.builder(opt)));
        CommandMetadata md = builder.build();

        Command mock = mock(Command.class);
        when(mock.run(any())).thenReturn(outcome);
        when(mock.getMetadata()).thenReturn(md);

        return mock;
    }

    @Before
    public void before() {
        this.mockCli = mock(Cli.class);
    }

    @Test
    public void testRun() {

        when(mockCli.commandName()).thenReturn("c1");

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
        verify(mockDefault, times(0)).run(mockCli);
        verify(mockHelp, times(0)).run(mockCli);
    }

    @Test
    public void testRun_ReverseOrder() {

        when(mockCli.commandName()).thenReturn("c2");

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC2).run(mockCli);
        verify(mockC1, times(0)).run(mockCli);
        verify(mockDefault, times(0)).run(mockCli);
        verify(mockHelp, times(0)).run(mockCli);
    }

    @Test(expected = IllegalStateException.class)
    public void testRun_NoMatch() {

        when(mockCli.commandName()).thenReturn("c3");

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
    }

    @Test
    public void testRun_NullName_Default() {

        when(mockCli.commandName()).thenReturn(null);

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1, times(0)).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
        verify(mockDefault).run(mockCli);
        verify(mockHelp, times(0)).run(mockCli);
    }

    @Test
    public void testRun_NullName_Help() {

        when(mockCli.commandName()).thenReturn(null);

        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.empty(), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1, times(0)).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
        verify(mockHelp).run(mockCli);
    }

    @Test
    public void testRun_NullName_NoFallback() {

        when(mockCli.commandName()).thenReturn(null);

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.empty(), Optional.empty(), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1, times(0)).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
    }

    @Test
    public void testRun_Failure() {

        when(mockCli.commandName()).thenReturn("c1");

        Command mockC1 = mockCommand("c1", CommandOutcome.failed(-1, "fff"), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.empty(), Optional.empty(), mockC1, mockC2);
        assertFalse(result.isSuccess());
        assertEquals(-1, result.getExitCode());
        assertEquals("fff", result.getMessage());
    }

    private CommandOutcome run(Optional<Command> defaultCommand, Optional<Command> helpCommand, Command... commands) {

        Map<String, Command> commandMap = new HashMap<>();
        asList(commands).forEach(c -> commandMap.put(c.getMetadata().getName(), c));
        CommandManager commandManager = new DefaultCommandManager(commandMap, defaultCommand, helpCommand);
        return new DefaultRunner(mockCli, commandManager).run();
    }
}
