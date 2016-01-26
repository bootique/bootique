package com.nhl.bootique.command;

import com.nhl.bootique.cli.OptionsBuilder;
import com.nhl.bootique.cli.Options;

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
	 * @since 1.12
	 */
	CommandOutcome run(Options options);

	/**
	 * Allows subclasses to configure visible CLI options.
	 * 
	 * @param optionsBuilder
	 *            a mutable builder of options for a given command.
	 */
	default void configOptions(OptionsBuilder optionsBuilder) {
		// do nothing by default...
	}
}
