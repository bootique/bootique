package io.bootique.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @since 0.12
 */
public class DefaultCommandManager implements CommandManager {

    private final Map<String, Command> commands;
    private final Optional<Command> defaultCommand;
    private final Optional<Command> helpCommand;

    private volatile Map<Class, Command> allCommandsByType;

    public DefaultCommandManager(Map<String, Command> commands,
                                 Optional<Command> defaultCommand,
                                 Optional<Command> helpCommand) {

        this.commands = commands;
        this.defaultCommand = defaultCommand;
        this.helpCommand = helpCommand;
    }

    @Override
    public Optional<Command> getHelpCommand() {
        return helpCommand;
    }

    @Override
    public Optional<Command> getDefaultCommand() {
        return defaultCommand;
    }

    @Override
    public Map<String, Command> getCommands() {
        return commands;
    }

    @Override
    public Command lookupByType(Class<? extends Command> commandType) {

        Command match = allCommandsByType().get(commandType);
        if (match == null) {
            throw new IllegalArgumentException("Unknown command type: " + commandType.getName());
        }
        return match;
    }

    @Override
    public Command lookupByName(String commandName) {
        Command match = commands.get(commandName);

        return match != null ? match : defaultCommand
                .filter(c -> c.getMetadata().getName().equals(commandName))
                .orElseThrow(() -> new IllegalArgumentException("Unknown command name: " + commandName));
    }

    private Map<Class, Command> allCommandsByType() {
        // lookup by class is an edge case used by command decorators and such, so create index on demand

        if (allCommandsByType == null) {
            synchronized (this) {
                if (allCommandsByType == null) {
                    allCommandsByType = createAllCommandsByType();
                }
            }
        }

        return allCommandsByType;
    }

    private Map<Class, Command> createAllCommandsByType() {

        Map<Class, Command> map = new HashMap<>();

        defaultCommand.ifPresent(c -> map.put(c.getClass(), c));
        commands.values().forEach(c -> map.put(c.getClass(), c));

        return map;
    }
}
