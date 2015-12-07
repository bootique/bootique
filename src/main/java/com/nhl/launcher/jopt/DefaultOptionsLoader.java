package com.nhl.launcher.jopt;

import java.util.List;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Key;

import com.nhl.launcher.BootstrapModule;
import com.nhl.launcher.command.Command;

import joptsimple.OptionParser;

public class DefaultOptionsLoader implements OptionsLoader {

	public static final String ARGS = "com.nhl.launcher.args";
	public static final Key<String[]> ARGS_KEY = Key.get(String[].class, ARGS);

	private String[] args;
	private List<Command> commands;

	public DefaultOptionsLoader(@Inject(BootstrapModule.COMMANDS_KEY) List<Command> commands,
			@Inject(ARGS) String[] args) {
		this.commands = commands;
		this.args = args;
	}

	@Override
	public Options loadOptions() {
		OptionParser parser = new OptionParser();

		// allow each command to add its own options before parsing
		commands.forEach(e -> e.configOptions(parser));

		return new Options(parser, parser.parse(args));
	}
}
