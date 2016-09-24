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
