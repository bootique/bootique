package com.nhl.bootique.jopt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.cli.CliOption;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandMetadata;
import com.nhl.bootique.command.CommandMetadata.Builder;
import com.nhl.bootique.log.BootLogger;

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
		return new JoptCliProvider(mockBootLogger, commands, options, argsArray).get();
	}

}
