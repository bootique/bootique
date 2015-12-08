package com.nhl.launcher.jopt;

import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.launcher.command.Command;

import joptsimple.OptionParser;

public class OptionsProvider implements Provider<Options> {

	private String[] args;
	private Collection<Command> commands;

	@Inject
	public OptionsProvider(Set<Command> commands, @Args String[] args) {
		this.commands = commands;
		this.args = args;
	}

	@Override
	public Options get() {
		OptionParser parser = new OptionParser();

		// allow each command to add its own options before parsing
		commands.forEach(e -> e.configOptions(parser));

		return new Options(parser, parser.parse(args));
	}
}
