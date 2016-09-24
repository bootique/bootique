package io.bootique.command;

import java.util.Map;
import java.util.Optional;

/**
 * @since 0.12
 */
public class DefaultCommandManager implements CommandManager {

    private Map<String, Command> commands;
    private Optional<Command> defaultCommand;
    private Optional<Command> helpCommand;

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
}
