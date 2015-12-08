package com.nhl.launcher.command;

import com.nhl.launcher.jopt.Options;

public class FailoverHelpCommand extends HelpCommand {

	@Override
	public CommandOutcome run(Options options) {
		return printHelp(options);
	}
}
