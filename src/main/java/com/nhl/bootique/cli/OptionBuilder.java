package com.nhl.bootique.cli;

/**
 * @since 0.12
 */
public interface OptionBuilder {

	OptionBuilder requiresArgument(String description);

	OptionBuilder mayTakeArgument(String description);
}
