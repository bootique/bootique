package com.nhl.bootique.command;

import com.nhl.bootique.cli.CommandLine;

public abstract class OptionTriggeredCommand implements Command {

	@Override
	public final CommandOutcome run(CommandLine options) {
		return hasOption(options) ? doRun(options) : CommandOutcome.skipped();
	}

	protected boolean hasOption(CommandLine options) {
		return options.hasOption(getOption());
	}

	protected abstract CommandOutcome doRun(CommandLine options);

	protected abstract String getOption();
}
