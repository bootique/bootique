package com.nhl.launcher;

import java.util.List;

import org.apache.cayenne.di.Inject;

import com.nhl.launcher.command.Command;
import com.nhl.launcher.command.CommandOutcome;
import com.nhl.launcher.jopt.Options;
import com.nhl.launcher.jopt.OptionsLoader;

public class DefaultRunner implements Runner {

	private OptionsLoader optionsLoader;
	private List<Command> commands;

	public DefaultRunner(@Inject OptionsLoader optionsLoader,
			@Inject(BootstrapModule.COMMANDS_KEY) List<Command> commands) {
		this.optionsLoader = optionsLoader;
		this.commands = commands;
	}

	public CommandOutcome run() {

		Options options = optionsLoader.loadOptions();

		for (Command c : commands) {

			CommandOutcome outcome = c.run(options);

			if (outcome.shouldExit()) {
				return outcome;
			}
		}

		return CommandOutcome.succeeded();
	}

}
