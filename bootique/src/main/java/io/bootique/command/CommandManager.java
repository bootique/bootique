package io.bootique.command;

import java.util.Map;
import java.util.Optional;

/**
 * A service interface that provides the rest of Bootique with access to
 * available commands.
 *
 * @since 0.12
 */
public interface CommandManager {

    /**
     * Returns all available commands excluding default.
     *
     * @return all available commands excluding default.
     * @since 0.20
     */
    Map<String, Command> getCommands();

    /**
     * Returns a command matching type out of all available commands including the default. Throws an exception if
     * the command type is not registered in the Bootique stack.
     *
     * @return a command matching the specified type.
     * @since 0.25
     */
    Command lookupByType(Class<? extends Command> commandType);

    /**
     * Returns a command by type out of all available commands including the default. Throws an exception if the command
     * type is not registered in the Bootique stack.
     *
     * @return a command matching the specified type.
     * @since 0.25
     */
    Command lookupByName(String commandName);

    /**
     * Returns optional default command.
     *
     * @return optional default command for this runtime.
     * @since 0.20
     */
    Optional<Command> getDefaultCommand();

    /**
     * Returns optional help command.
     *
     * @return optional help command for this runtime.
     * @since 0.20
     */
    Optional<Command> getHelpCommand();
}
