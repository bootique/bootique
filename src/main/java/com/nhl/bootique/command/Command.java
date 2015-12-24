package com.nhl.bootique.command;

import com.nhl.bootique.jopt.Options;

import joptsimple.OptionParser;

@FunctionalInterface
public interface Command {

	/**
	 * Executes a command.
	 * 
	 * @param options
	 *            command-line options object.
	 * @return CommandOutcome object that indicates to the caller whether
	 *         command was successful and whether it needs to continue with
	 *         command chain.
	 */
	CommandOutcome run(Options options);

	default void configOptions(OptionParser parser) {
		// do nothing by default...
	}
}
