package com.nhl.bootique.command;

import com.google.inject.Inject;
import com.nhl.bootique.cli.Options;
import com.nhl.bootique.log.BootLogger;

/**
 * Prints help regardless of whether the help option was requested or not.
 * Intended to be used as a failover command when no other command was
 * explicitly specified.
 */
public class FailoverHelpCommand extends HelpCommand {

	@Inject
	public FailoverHelpCommand(BootLogger bootLogger) {
		super(bootLogger);
	}

	@Override
	public CommandOutcome run(Options options) {
		return printHelp(options);
	}
}
