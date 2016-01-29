package com.nhl.bootique.command;

import com.nhl.bootique.cli.Cli;

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
	 * @since 0.12
	 */
	CommandOutcome run(Cli options);

	/**
	 * Returns a metadata object for this command. Default implementation
	 * generates barebone metadata based on class name.
	 * 
	 * @return metadata object describing current command.
	 * @since 0.12
	 */
	default CommandMetadata getMetadata() {
		return CommandMetadata.builder(getClass()).build();
	}
}
