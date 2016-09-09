package io.bootique.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.bootique.cli.Cli;
import io.bootique.cli.meta.CliOption;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandMetadata;
import io.bootique.command.CommandMetadata.Builder;
import io.bootique.command.CommandOutcome;
import io.bootique.command.DefaultCommandManager;

public class DefaultRunnerTest {

	private Command mockDefaultCommand;

	private Cli mockCli;

	@Before
	public void before() {

		this.mockDefaultCommand = createMockCommand("d1", CommandOutcome.succeeded());
		this.mockCli = mock(Cli.class);
	}

	@Test
	public void testRun() {

		when(mockCli.commandName()).thenReturn("c1");

		Command mockC1 = createMockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
		Command mockC2 = createMockCommand("c2", CommandOutcome.succeeded(), "c2o1");

		CommandOutcome result = run(mockC1, mockC2);
		assertTrue(result.isSuccess());

		verify(mockC1).run(mockCli);
		verify(mockC2, times(0)).run(mockCli);
		verify(mockDefaultCommand, times(0)).run(mockCli);
	}

	@Test
	public void testRun_ReverseOrder() {

		when(mockCli.commandName()).thenReturn("c2");

		Command mockC1 = createMockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
		Command mockC2 = createMockCommand("c2", CommandOutcome.succeeded(), "c2o1");

		CommandOutcome result = run(mockC1, mockC2);
		assertTrue(result.isSuccess());

		verify(mockC2).run(mockCli);
		verify(mockC1, times(0)).run(mockCli);
		verify(mockDefaultCommand, times(0)).run(mockCli);
	}

	@Test
	public void testRun_NoMatch() {

		when(mockCli.commandName()).thenReturn("c3");

		Command mockC1 = createMockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
		Command mockC2 = createMockCommand("c2", CommandOutcome.succeeded(), "c2o1");

		CommandOutcome result = run(mockC1, mockC2);
		assertTrue(result.isSuccess());

		verify(mockDefaultCommand).run(mockCli);
		verify(mockC1, times(0)).run(mockCli);
		verify(mockC2, times(0)).run(mockCli);
	}

	@Test
	public void testRun_NullName() {

		when(mockCli.commandName()).thenReturn(null);

		Command mockC1 = createMockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
		Command mockC2 = createMockCommand("c2", CommandOutcome.succeeded(), "c2o1");

		CommandOutcome result = run(mockC1, mockC2);
		assertTrue(result.isSuccess());

		verify(mockDefaultCommand).run(mockCli);
		verify(mockC1, times(0)).run(mockCli);
		verify(mockC2, times(0)).run(mockCli);
	}

	@Test
	public void testRun_Failure() {

		when(mockCli.commandName()).thenReturn("c1");

		Command mockC1 = createMockCommand("c1", CommandOutcome.failed(-1, "fff"), "c1o1", "c1o2");
		Command mockC2 = createMockCommand("c2", CommandOutcome.succeeded(), "c2o1");

		CommandOutcome result = run(mockC1, mockC2);
		assertFalse(result.isSuccess());
		assertEquals(-1, result.getExitCode());
		assertEquals("fff", result.getMessage());
	}

	private CommandOutcome run(Command... commands) {
		Set<Command> commandSet = new HashSet<>(Arrays.asList(commands));
		CommandManager commandManager = DefaultCommandManager.create(commandSet, mockDefaultCommand);

		return new DefaultRunner(mockCli, commandManager).run();
	}

	private Command createMockCommand(String name, CommandOutcome outcome, String... options) {

		Command mock = mock(Command.class);

		when(mock.run(any())).thenReturn(outcome);

		// for now JopCLiProvider adds command name as an option;
		// using this option in command line would match the original command
		// name

		Builder builder = CommandMetadata.builder(name);
		Arrays.asList(options).forEach(opt -> builder.addOption(CliOption.builder(opt)));

		CommandMetadata md = builder.build();

		when(mock.getMetadata()).thenReturn(md);

		return mock;
	}
}
