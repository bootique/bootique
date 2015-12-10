package com.nhl.bootique.command;

import com.nhl.bootique.jopt.Options;

public class FailoverHelpCommand extends HelpCommand {

	@Override
	public CommandOutcome run(Options options) {
		return printHelp(options);
	}
}
