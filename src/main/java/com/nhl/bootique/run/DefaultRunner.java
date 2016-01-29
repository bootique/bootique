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

	@Inject
	public DefaultRunner(Cli cli, Set<Command> commands, @DefaultCommand Command defaultCommand) {
		this.cli = cli;
		this.commands = commands;
		this.defaultCommand = defaultCommand;
	}

	@Override
	public CommandOutcome run() {

		for (Command c : commands) {

			CommandOutcome outcome = c.run(cli);

			if (outcome.shouldExit()) {
				return outcome;
			}
		}

		return defaultCommand.run(cli);
	}

}
