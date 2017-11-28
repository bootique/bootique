package io.bootique.command;

/**
 * An internal data structure that denotes a reference to a command combined with that command decorator.
 *
 * @since 0.25
 */
public class CommandRefDecorated {

    private Class<? extends Command> commandType;
    private CommandDecorator decorator;

    public CommandRefDecorated(Class<? extends Command> commandType, CommandDecorator decorator) {
        this.commandType = commandType;
        this.decorator = decorator;
    }

    public Class<? extends Command> getCommandType() {
        return commandType;
    }

    public CommandDecorator getDecorator() {
        return decorator;
    }
}
