package com.nhl.bootique.command;

import com.nhl.bootique.cli.Cli;

public class ConfigCommand extends CommandWithMetadata {

	public static final String CONFIG_OPTION = "config";

	public ConfigCommand() {
		super(CommandMetadata.builder(ConfigCommand.class).description("Specifies YAML config file path.").build());
	}

	@Override
	public CommandOutcome run(Cli cli) {
		return CommandOutcome.skipped();
	}
}
