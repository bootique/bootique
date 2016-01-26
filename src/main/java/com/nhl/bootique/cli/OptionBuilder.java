package com.nhl.bootique.cli;

/**
 * @since 0.12
 */
public interface OptionBuilder {

	void requiresArgument(String description);

	void mayTakeArgument(String description);

}
