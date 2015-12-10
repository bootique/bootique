package com.nhl.bootique.command;

import java.util.Map;

import com.nhl.bootique.jopt.Options;

import joptsimple.OptionParser;
import joptsimple.OptionSpec;

public class ConfigCommand implements Command {

	public static final String CONFIG_OPTION = "config";

	@Override
	public CommandOutcome run(Options options) {
		return CommandOutcome.skipped();
	}

	@Override
	public void configOptions(OptionParser parser) {

		// install framework options unless they are already defined...

		Map<String, OptionSpec<?>> existing = parser.recognizedOptions();

		if (!existing.containsKey(CONFIG_OPTION)) {
			parser.accepts(CONFIG_OPTION, "Specifies YAML config file path.").withRequiredArg()
					.describedAs("config_file");
		}

	}
}
