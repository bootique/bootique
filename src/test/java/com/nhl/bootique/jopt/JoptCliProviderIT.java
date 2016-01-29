package com.nhl.bootique.jopt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.cli.CliOption;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandMetadata;
import com.nhl.bootique.log.BootLogger;

public class JoptCliProviderIT {

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
	public void testGet_HasOption() {

		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me")));

		assertTrue(createOptions("-m").hasOption("me"));
		assertTrue(createOptions("--me").hasOption("me"));
		assertFalse(createOptions("-m").hasOption("not_me"));
		assertFalse(createOptions("-m").hasOption("m"));
	}

	@Test
	public void testOptionStrings_Short() {
		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null)));

		assertEquals(createOptions("-m v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Long_Equals() {
		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null)));

		assertEquals(createOptions("--me=v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Long_Space() {
		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null)));

		assertEquals(createOptions("--me v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Single_Mixed() {

		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null))
				.addOption(CliOption.builder("other").valueOptional(null)));

		assertEquals(createOptions("--other v2 --me=v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Multiple_Mixed() {
		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null))
				.addOption(CliOption.builder("other").valueOptional(null))
				.addOption(CliOption.builder("n").valueOptional(null)).addOption(CliOption.builder("yes")));

		assertEquals(createOptions("--me=v1 --other v2 -n v3 --me v4 --yes").optionStrings("me"), "v1", "v4");
	}

	@Test
	public void testNonOptionArgs_Mix() {
		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null))
				.addOption(CliOption.builder("other").valueOptional(null)).addOption(CliOption.builder("yes")));

		assertEquals(createOptions("a --me=v1 --other v2 b --me v4 --yes c d").standaloneArguments(), "a", "b", "c",
				"d");
	}

	@Test
	public void testNonOptionArgs_DashDash() {
		addMockCommand(CommandMetadata.builder("c1").addOption(CliOption.builder("me").valueOptional(null))
				.addOption(CliOption.builder("other").valueOptional(null)));

		assertEquals(createOptions("a --me=v1 -- --other v2").standaloneArguments(), "a", "--other", "v2");
	}

	private void assertEquals(Collection<String> result, String... expected) {
		assertArrayEquals(expected, result.toArray());
	}

	private Command addMockCommand(CommandMetadata.Builder metadataBuilder) {
		Command mock = mock(Command.class);
		when(mock.getMetadata()).thenReturn(metadataBuilder.build());
		commands.add(mock);
		return mock;
	}

	private Cli createOptions(String args) {
		String[] argsArray = args.split(" ");
		return new JoptCliProvider(mockBootLogger, commands, options, argsArray).get();
	}
}
