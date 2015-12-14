package com.nhl.bootique.command;

import com.google.inject.Inject;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.log.BootLogger;

public class FailoverHelpCommand extends HelpCommand {

	@Inject
	public FailoverHelpCommand(BootLogger bootLogger) {
		super(bootLogger);
		// TODO Auto-generated constructor stub
	}

	@Override
	public CommandOutcome run(Options options) {
		return printHelp(options);
	}
}
