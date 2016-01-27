package com.nhl.bootique.run;

import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;
import com.nhl.bootique.cli.CommandLine;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.command.DefaultCommand;

public class DefaultRunner implements Runner {

	private CommandLine options;
	private Collection<Command> commands;
	private Command defaultCommand;

	@Inject
	public DefaultRunner(CommandLine options, Set<Command> commands, @DefaultCommand Command defaultCommand) {
		this.options = options;
		this.commands = commands;
		this.defaultCommand = defaultCommand;
	}

	@Override
	public CommandOutcome run() {

		for (Command c : commands) {

			CommandOutcome outcome = c.run(options);

			if (outcome.shouldExit()) {
				return outcome;
			}
		}

		return defaultCommand.run(options);
	}

}
