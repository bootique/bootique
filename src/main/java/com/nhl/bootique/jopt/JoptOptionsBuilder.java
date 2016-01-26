package com.nhl.bootique.jopt;

import java.util.Optional;

import com.nhl.bootique.cli.OptionBuilder;
import com.nhl.bootique.cli.OptionsBuilder;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSpecBuilder;

/**
 * @since 0.12
 */
class JoptOptionsBuilder implements OptionsBuilder {

	// an option builder that ignores everything passed to it...
	private static final OptionBuilder BLOCKED_OPTION_BUILDER = new OptionBuilder() {

		@Override
		public void requiresArgument(String description) {
			// do nothing
		}

		@Override
		public void mayTakeArgument(String description) {
			// do nothing
		}
	};

	private OptionParser optionParser;
	private BootLogger bootLogger;

	JoptOptionsBuilder(OptionParser optionParser, BootLogger bootLogger) {
		this.optionParser = optionParser;
		this.bootLogger = bootLogger;
	}

	JoptOptions build(String... args) {
		return new JoptOptions(bootLogger, optionParser, optionParser.parse(args));
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
