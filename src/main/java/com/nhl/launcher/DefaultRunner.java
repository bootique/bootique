package com.nhl.launcher;

import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;
import com.nhl.launcher.command.Command;
import com.nhl.launcher.command.CommandOutcome;
import com.nhl.launcher.command.DefaultCommand;
import com.nhl.launcher.jopt.Options;

public class DefaultRunner implements Runner {

	private Options options;
	private Collection<Command> commands;
	private Command defaultCommand;

	@Inject
	public DefaultRunner(Options options, Set<Command> commands, @DefaultCommand Command defaultCommand) {
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
