package io.bootique.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A service interface that provides the rest of Bootique with access to available commands.
 *
 * @since 0.12
 */
public interface CommandManager {

    /**
     * Returns all public commands excluding the default.
     *
     * @return all public commands excluding default.
     * @since 0.25
     * @deprecated since 0.25 use {@link #getAllCommands()} and filter the result accordingly.
     */
    @Deprecated
    default Map<String, Command> getCommands() {

        Map<String, ManagedCommand> allCommands = getAllCommands();
        Map<String, Command> publicNonDefault = new HashMap<>((int) (allCommands.size() / 0.75));

        allCommands.forEach((n, mc) -> {
            if (mc.isPublic() && !mc.isDefault()) {
                publicNonDefault.put(n, mc.getCommand());
            }
        });

        return publicNonDefault;
    }

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
     * Returns optional default command.
     *
     * @return optional default command for this runtime.
     * @since 0.20
     * @deprecated since 0.25 in favor of {@link #getPublicDefaultCommand()}.
     */
    @Deprecated
    default Optional<Command> getDefaultCommand() {
        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isDefault()) {
                return Optional.of(mc.getCommand());
            }
        }

        return Optional.empty();
    }

    /**
     * Returns optional public default command.
     *
     * @return optional public default command for this runtime.
     * @since 0.25
     */
    default Optional<Command> getPublicDefaultCommand() {

        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isDefault() && mc.isPublic()) {
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
     * @deprecated since 0.25 in favor of {@link #getPublicHelpCommand()}.
     */
    @Deprecated
    default Optional<Command> getHelpCommand() {
        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isHelp()) {
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
            if (mc.isHelp() && mc.isPublic()) {
                return Optional.of(mc.getCommand());
            }
        }

        return Optional.empty();
    }
}
