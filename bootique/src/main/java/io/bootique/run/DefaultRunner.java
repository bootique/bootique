package io.bootique.run;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandOutcome;

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
