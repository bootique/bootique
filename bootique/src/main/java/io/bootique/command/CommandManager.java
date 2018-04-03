package io.bootique.command;

import java.util.Map;
import java.util.Optional;

/**
 * A service interface that provides the rest of Bootique with access to available commands.
 *
 * @since 0.12
 */
public interface CommandManager {

    /**
     * Returns a map of {@link ManagedCommand} instances by command name, including all known commands: public, private,
     * default, help.
     *
     * @return a map of {@link ManagedCommand} instances by command name.
     * @since 0.25
     */
    Map<String, ManagedCommand> getAllCommands();

    /**
     * Returns a command matching the type. Throws an exception if the command type is not registered in the Bootique stack.
     *
     * @return a {@link ManagedCommand} matching the specified type.
     * @since 0.25
     */
    ManagedCommand lookupByType(Class<? extends Command> commandType);

    /**
     * Returns a command matching the name. Throws an exception if the command type is not registered in the Bootique stack.
     *
     * @return a {@link ManagedCommand} matching the specified type.
     * @since 0.25
     */
    default ManagedCommand lookupByName(String commandName) {
        ManagedCommand match = getAllCommands().get(commandName);

        if (match == null) {
            throw new IllegalArgumentException("Unknown command name: " + commandName);
        }

        return match;
    }

    /**
     * Returns optional public default command.
     *
     * @return optional public default command for this runtime.
     * @since 0.25
     */
    default Optional<Command> getPublicDefaultCommand() {

        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isDefault() && !mc.isHidden()) {
                return Optional.of(mc.getCommand());
            }
        }

        return Optional.empty();
    }

    /**
     * Returns optional help command.
     *
     * @return optional help command for this runtime.
     * @since 0.20
     */
    default Optional<Command> getPublicHelpCommand() {
        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isHelp() && !mc.isHidden()) {
                return Optional.of(mc.getCommand());
            }
        }

        return Optional.empty();
    }
}
