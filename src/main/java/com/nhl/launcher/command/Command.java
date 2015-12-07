package com.nhl.launcher.command;

import com.nhl.launcher.jopt.Options;

import joptsimple.OptionParser;

@FunctionalInterface
public interface Command {

	/**
	 * Executes a command.
	 * 
	 * @return CommandOutcome object that indicates to the caller whether
	 *         command was successful and whether it needs to continue with
	 *         command chain.
	 */
	CommandOutcome run(Options options);

	default void configOptions(OptionParser parser) {
		// do nothing by default...
	}
}
