package com.nhl.launcher.command;

import com.nhl.launcher.jopt.Options;

import joptsimple.OptionParser;

public abstract class OptionTriggeredCommand implements Command {

	@Override
	public final CommandOutcome run(Options options) {
		return hasOption(options) ? doRun(options) : CommandOutcome.skipped();
	}

	@Override
	public void configOptions(OptionParser parser) {
		parser.accepts(getOption());
	}

	protected boolean hasOption(Options options) {
		return options.getOptionSet().has(getOption());
	}

	protected abstract CommandOutcome doRun(Options options);

	protected abstract String getOption();
}
