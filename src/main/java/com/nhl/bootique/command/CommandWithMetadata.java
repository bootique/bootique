package com.nhl.bootique.command;

import com.nhl.bootique.cli.Cli;

/**
 * An abstract superlcass of commands that provide their own metadata.
 * 
 * @since 0.12
 */
public abstract class CommandWithMetadata implements Command {

	private CommandMetadata metadata;

	public CommandWithMetadata(CommandMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public CommandMetadata getMetadata() {
		return metadata;
	}

	@Override
	public abstract CommandOutcome run(Cli cli);
}
