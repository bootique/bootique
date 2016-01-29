package com.nhl.bootique.jopt;

import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.bootique.annotation.Args;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.cli.CliOption;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class JoptCliProvider implements Provider<Cli> {

	private String[] args;
	private Map<String, Command> commands;
	private Set<CliOption> options;
	private BootLogger bootLogger;

	@Inject
	public JoptCliProvider(BootLogger bootLogger, Set<Command> commands, Set<CliOption> options, @Args String[] args) {
		this.commands = mapCommands(commands);
		this.options = options;
		this.args = args;
		this.bootLogger = bootLogger;
	}

	@Override
	public Cli get() {
		OptionParser parser = createParser();
		OptionSet parsed = parser.parse(args);
		String commandName = commandName(parsed);

		return new JoptCli(bootLogger, parser, parsed, commandName);
	}

	protected Map<String, Command> mapCommands(Set<Command> commands) {
		Map<String, Command> map = new HashMap<>();

		commands.forEach(c -> {

			String name = c.getMetadata().getName();
			Command existing = map.put(name, c);
			if (existing != null) {
				// TODO: BootiqueException?
				String message = String.format("Ambiguos command name: %s (shared by at least 2 commands: %s, %s)",
						name, c.getClass().getName(), existing.getClass().getName());
				throw new RuntimeException(message);
			}
		});

		return map;
	}

	protected OptionParser createParser() {
		OptionParser parser = new OptionParser();

		commands.values().forEach(c -> {

			c.getMetadata().getOptions().forEach(o -> {
				addOption(parser, o);
			});

			// using option-bound command strategy...
			addOption(parser, CliOption.builder(c.getMetadata().getName()).build());
		});
		
		// load global options; TODO: check for conflicts with other options
		options.forEach(o -> addOption(parser, o));

		return parser;
	}

	private void addOption(OptionParser parser, CliOption option) {

		OptionSpecBuilder optionBuilder = parser.accepts(option.getName(), option.getDescription());
		switch (option.getValueCardinality()) {
		case OPTIONAL:
			optionBuilder.withOptionalArg().describedAs(option.getValueDescription());
			break;
		case REQUIRED:
			optionBuilder.withRequiredArg().describedAs(option.getValueDescription());
		default:
			break;
		}
	}

	// using option-bound command strategy...
	protected String commandName(OptionSet optionSet) {

		Map<String, Command> matches = new HashMap<>(3);
		commands.forEach((k, v) -> {
			if (optionSet.has(k) && !optionSet.hasArgument(k)) {
				matches.put(k, v);
			}
		});

		switch (matches.size()) {
		case 0:
			// default command should be invoked
			return null;
		case 1:
			return matches.keySet().iterator().next();
		default:
			String opts = matches.keySet().stream().collect(joining(", "));
			String message = String.format("Ambiguos options, matched multiple commands: %s", opts);

			// TODO: BootiqueException?
			throw new RuntimeException(message);
		}
	}
}
