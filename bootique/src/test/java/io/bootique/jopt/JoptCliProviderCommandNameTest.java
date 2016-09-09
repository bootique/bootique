package io.bootique.jopt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
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
import io.bootique.command.DefaultCommandManager;
import io.bootique.command.CommandMetadata.Builder;
import io.bootique.log.BootLogger;

public class JoptCliProviderCommandNameTest {

	private BootLogger mockBootLogger;
	private Set<Command> commands;
	private Set<CliOption> options;

	@Before
	public void before() {
		this.mockBootLogger = mock(BootLogger.class);
		this.commands = new HashSet<>();
		this.options = new HashSet<>();
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

		Builder builder = CommandMetadata.builder(name);
		Arrays.asList(options).forEach(opt -> builder.addOption(CliOption.builder(opt)));

		CommandMetadata md = builder.build();

		when(mock.getMetadata()).thenReturn(md);
		commands.add(mock);
	}

	private Cli createCli(String args) {
		String[] argsArray = args.split(" ");

		Command mockDefaultCommand = mock(Command.class);
		CommandManager commandManager = DefaultCommandManager.create(commands, mockDefaultCommand);
		return new JoptCliProvider(mockBootLogger, commandManager, options, argsArray).get();
	}

}
