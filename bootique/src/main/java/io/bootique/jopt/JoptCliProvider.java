package io.bootique.jopt;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.annotation.Args;
import io.bootique.application.ApplicationMetadata;
import io.bootique.application.OptionMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.log.BootLogger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class JoptCliProvider implements Provider<Cli> {

    private String[] args;
    private BootLogger bootLogger;
    private ApplicationMetadata application;
    private CommandManager commandManager;

    @Inject
    public JoptCliProvider(BootLogger bootLogger,
                           CommandManager commandManager,
                           ApplicationMetadata application,
                           @Args String[] args) {

        this.commandManager = commandManager;
        this.application = application;
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

        application.getCommands().forEach(c -> {

            c.getOptions().forEach(o -> {
                addOption(parser, o);
            });

            // using option-bound command strategy...
            addOption(parser, OptionMetadata.builder(c.getName()).description(c.getDescription()).build());
        });

        // load global options
        application.getOptions().forEach(o -> addOption(parser, o));
        return parser;
    }

    protected void addOption(OptionParser parser, OptionMetadata option) {

        // ensure non-null description
        String description = Optional.ofNullable(option.getDescription()).orElse("");

        OptionSpecBuilder optionBuilder = parser.accepts(option.getName(), description);
        switch (option.getValueCardinality()) {
            case OPTIONAL:
                optionBuilder.withOptionalArg().describedAs(option.getValueName());
                break;
            case REQUIRED:
                optionBuilder.withRequiredArg().describedAs(option.getValueName());
            default:
                break;
        }
    }

    // using option-bound command strategy...
    protected String commandName(OptionSet optionSet) {

        Map<String, Command> matches = new HashMap<>(3);
        commandManager.getCommands().forEach((name, c) -> {
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
                String message = String.format("Ambiguous options, matched multiple commands: %s", opts);

                // TODO: BootiqueException?
                throw new RuntimeException(message);
        }
    }
}
