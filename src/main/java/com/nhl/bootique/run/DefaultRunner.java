package com.nhl.bootique.run;

import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;
import com.nhl.bootique.annotation.DefaultCommand;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;

public class DefaultRunner implements Runner {

	private Cli cli;
	private Collection<Command> commands;
	private Command defaultCommand;

	// TODO: inject commands as map by name
	@Inject
	public DefaultRunner(Cli cli, Set<Command> commands, @DefaultCommand Command defaultCommand) {
		this.cli = cli;
		this.commands = commands;
		this.defaultCommand = defaultCommand;
	}

	@Override
	public CommandOutcome run() {
		return getCommand().run(cli);
	}

	// TODO: inject commands as map by name
	private Command getCommand() {
		if (cli.commandName() == null) {
			return defaultCommand;
		}

		// TODO: inject commands as map by name. This will ensure uniqueness and
		// easy lookup
		for (Command c : commands) {
			if (cli.commandName().equals(c.getMetadata().getName())) {
				return c;
			}
		}

		return defaultCommand;
	}

}
