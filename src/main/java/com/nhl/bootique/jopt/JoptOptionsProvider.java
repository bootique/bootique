package com.nhl.bootique.jopt;

import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.bootique.cli.Options;
import com.nhl.bootique.cli.OptionsBuilder;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;

public class JoptOptionsProvider implements Provider<Options> {

	private String[] args;
	private Collection<Command> commands;
	private BootLogger bootLogger;

	@Inject
	public JoptOptionsProvider(BootLogger bootLogger, Set<Command> commands, @Args String[] args) {
		this.commands = commands;
		this.args = args;
		this.bootLogger = bootLogger;
	}

	@Override
	public Options get() {
		OptionParser parser = new OptionParser();
		OptionsBuilder builder = new JoptOptionsBuilder(parser);

		// allow each command to add its own options before parsing
		commands.forEach(e -> e.configOptions(builder));

		return new JoptOptions(bootLogger, parser, parser.parse(args));
	}
}
