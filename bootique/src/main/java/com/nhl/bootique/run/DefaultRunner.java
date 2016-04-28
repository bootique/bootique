package com.nhl.bootique.run;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandManager;
import com.nhl.bootique.command.CommandOutcome;

public class DefaultRunner implements Runner {

	private Cli cli;
	private CommandManager commandManager;

	public DefaultRunner(Cli cli, CommandManager commandManager) {
		this.cli = cli;
		this.commandManager = commandManager;
	}

	@Override
	public CommandOutcome run() {
		return getCommand().run(cli);
	}

	private Command getCommand() {
		return commandManager.getCommand(cli.commandName());
	}

}
