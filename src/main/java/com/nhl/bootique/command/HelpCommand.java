package com.nhl.bootique.command;

import java.io.StringWriter;

import com.google.inject.Inject;
import com.nhl.bootique.cli.Options;
import com.nhl.bootique.cli.OptionsBuilder;
import com.nhl.bootique.log.BootLogger;

public class HelpCommand implements Command {

	protected static final String HELP_OPTION = "help";

	private BootLogger bootLogger;

	@Inject
	public HelpCommand(BootLogger bootLogger) {
		this.bootLogger = bootLogger;
	}

	@Override
	public CommandOutcome run(Options options) {
		return options.hasOption(HELP_OPTION) ? printHelp(options) : CommandOutcome.skipped();
	}

	protected CommandOutcome printHelp(Options options) {
		StringWriter out = new StringWriter();
		options.printHelp(out);

		bootLogger.stdout(out.toString());
		return CommandOutcome.succeeded();
	}

	@Override
	public void configOptions(OptionsBuilder optionsBuilder) {
		optionsBuilder.addHelp(HELP_OPTION, "Prints this message.");
	}

}
