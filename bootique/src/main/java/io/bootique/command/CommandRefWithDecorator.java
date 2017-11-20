package io.bootique.command;

/**
 * @since 0.25
 */
public class CommandRefWithDecorator {

    private Class<? extends Command> commandType;
    private CommandDecorator decorator;

    public CommandRefWithDecorator(Class<? extends Command> commandType, CommandDecorator decorator) {
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
