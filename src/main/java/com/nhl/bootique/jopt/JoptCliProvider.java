package com.nhl.bootique.jopt;

import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.bootique.annotation.Args;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.cli.CliOption;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandManager;
import com.nhl.bootique.command.CommandMetadata;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

public class JoptCliProvider implements Provider<Cli> {

	private String[] args;
	private Set<CliOption> options;
	private BootLogger bootLogger;
	private CommandManager commandManager;

	@Inject
	public JoptCliProvider(BootLogger bootLogger, CommandManager commandManager, Set<CliOption> options,
			@Args String[] args) {
		this.commandManager = commandManager;
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

	protected OptionParser createParser() {
		OptionParser parser = new OptionParser();

		commandManager.getCommands().forEach(c -> {

			CommandMetadata md = c.getMetadata();

			md.getOptions().forEach(o -> {
				addOption(parser, o);
			});

			// using option-bound command strategy...

			addOption(parser, CliOption.builder(md.getName()).description(md.getDescription()).build());
		});

		// load global options; TODO: check for conflicts with other options
		options.forEach(o -> addOption(parser, o));

		return parser;
	}

	protected void addOption(OptionParser parser, CliOption option) {

		// ensure non-null description
		String description = Optional.ofNullable(option.getDescription()).orElse("");

		OptionSpecBuilder optionBuilder = parser.accepts(option.getName(), description);
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
		commandManager.getCommands().forEach(c -> {
			String name = c.getMetadata().getName();
			if (optionSet.has(name) && !optionSet.hasArgument(name)) {
				matches.put(name, c);
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
