package com.nhl.bootique.command;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.bootique.jopt.Options;

import joptsimple.OptionParser;
import joptsimple.OptionSpec;

public class HelpCommand implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(HelpCommand.class);

	protected static final String HELP_OPTION = "help";

	@Override
	public CommandOutcome run(Options options) {
		return options.getOptionSet().has(HELP_OPTION) ? printHelp(options) : CommandOutcome.skipped();
	}

	protected CommandOutcome printHelp(Options options) {
		StringWriter out = new StringWriter();

		try {
			options.getParser().printHelpOn(out);
		} catch (IOException e) {
			LOGGER.warn("Error printing help", e);
		}

		System.out.println(out.toString());
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
