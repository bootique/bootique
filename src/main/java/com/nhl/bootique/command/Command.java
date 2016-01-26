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
	 *         command was successful and whether the caller needs to continue
	 *         with command chain.
	 */
	CommandOutcome run(Options options);

	/**
	 * Allows subclasses to configure visible CLI options.
	 * 
	 * @param parser
	 *            a mutable CLI parser that stores supported command line
	 *            options.
	 */
	default void configOptions(OptionParser parser) {
		// do nothing by default...
	}
}
