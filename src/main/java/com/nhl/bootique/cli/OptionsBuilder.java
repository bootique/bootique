package com.nhl.bootique.cli;

/**
 * @since 0.12
 */
public interface OptionsBuilder {

	OptionBuilder add(String option, String description);
	
	OptionBuilder addHelp(String option, String description);
}
