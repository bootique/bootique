package com.nhl.bootique.jopt;

import com.nhl.bootique.cli.OptionBuilder;

import joptsimple.OptionSpecBuilder;

/**
 * @since 0.12
 */
class JoptOptionBuilder implements OptionBuilder {

	private OptionSpecBuilder optionSpecBuilder;

	JoptOptionBuilder(OptionSpecBuilder optionSpecBuilder) {
		this.optionSpecBuilder = optionSpecBuilder;
	}

	@Override
	public OptionBuilder mayTakeArgument(String description) {
		optionSpecBuilder.withOptionalArg().describedAs(description);
		return this;
	}

	@Override
	public OptionBuilder requiresArgument(String description) {
		optionSpecBuilder.withRequiredArg().describedAs(description);
		return this;
	}

}
