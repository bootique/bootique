package com.nhl.launcher.command;

import com.nhl.launcher.jopt.Options;

public abstract class OptionTriggeredCommand implements Command {

	@Override
	public final CommandOutcome run(Options options) {
		return hasOption(options) ? doRun(options) : CommandOutcome.skipped();
	}

	protected boolean hasOption(Options options) {
		return options.getOptionSet().has(getOption());
	}

	protected abstract CommandOutcome doRun(Options options);

	protected abstract String getOption();
}
