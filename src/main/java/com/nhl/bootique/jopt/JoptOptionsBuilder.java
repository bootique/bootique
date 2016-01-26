package com.nhl.bootique.jopt;

import java.util.Optional;

import com.nhl.bootique.cli.OptionBuilder;
import com.nhl.bootique.cli.OptionsBuilder;

import joptsimple.OptionParser;
import joptsimple.OptionSpecBuilder;

/**
 * @since 0.12
 */
class JoptOptionsBuilder implements OptionsBuilder {

	// an option builder that ignores everything passed to it...
	private static final OptionBuilder BLOCKED_OPTION_BUILDER = new OptionBuilder() {

		@Override
		public OptionBuilder requiresArgument(String description) {
			return this;
		}

		@Override
		public OptionBuilder mayTakeArgument(String description) {
			return this;
		}
	};

	private OptionParser optionParser;

	JoptOptionsBuilder(OptionParser optionParser) {
		this.optionParser = optionParser;
	}

	private Optional<OptionSpecBuilder> specBuilder(String option, String description) {
		return optionParser.recognizedOptions().containsKey(option) ? Optional.empty()
				: Optional.of(optionParser.accepts(option, description));
	}

	@Override
	public OptionBuilder add(String option, String description) {
		Optional<OptionSpecBuilder> specBuilder = specBuilder(option, description);
		return specBuilder.isPresent() ? new JoptOptionBuilder(specBuilder.get()) : BLOCKED_OPTION_BUILDER;
	}

	@Override
	public OptionBuilder addHelp(String option, String description) {

		Optional<OptionSpecBuilder> specBuilder = specBuilder(option, description);

		if (!specBuilder.isPresent()) {
			return BLOCKED_OPTION_BUILDER;
		}

		specBuilder.get().forHelp();
		return new JoptOptionBuilder(specBuilder.get());
	}
}
