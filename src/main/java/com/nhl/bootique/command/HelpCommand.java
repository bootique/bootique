package com.nhl.bootique.command;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.google.inject.Inject;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSpec;

public class HelpCommand implements Command {

	protected static final String HELP_OPTION = "help";

	private BootLogger bootLogger;

	@Inject
	public HelpCommand(BootLogger bootLogger) {
		this.bootLogger = bootLogger;
	}

	@Override
	public CommandOutcome run(Options options) {
		return options.getOptionSet().has(HELP_OPTION) ? printHelp(options) : CommandOutcome.skipped();
	}

	protected CommandOutcome printHelp(Options options) {
		StringWriter out = new StringWriter();

		try {
			options.getParser().printHelpOn(out);
		} catch (IOException e) {
			bootLogger.stderr("Error printing help", e);
		}

		bootLogger.stdout(out.toString());
		return CommandOutcome.succeeded();
	}

	@Override
	public void configOptions(OptionParser parser) {

		// install framework options unless they are already defined...
		Map<String, OptionSpec<?>> existing = parser.recognizedOptions();

		if (!existing.containsKey(HELP_OPTION)) {
			parser.accepts(HELP_OPTION, "Prints this message.").forHelp();
		}
	}

}
