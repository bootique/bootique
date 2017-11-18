package io.bootique.command;

import java.util.Objects;

/**
 * Stores a reference to a command and a set of invocation arguments. Used to capture auxiliary commands to be run with
 * the main command.
 *
 * @since 0.25
 */
public abstract class CommandWithArgs {

    private final String[] args;
    private final boolean terminateOnErrors;

    protected CommandWithArgs(String[] args, boolean terminateOnErrors) {
        this.args = args;
        this.terminateOnErrors = terminateOnErrors;
    }

    /**
     * Starts building an invocation with a named command.
     *
     * @param fullCommandName full name of the command.
     */
    public static Builder forName(String fullCommandName) {
        return new Builder(fullCommandName);
    }

    /**
     * Starts building an invocation with a command of a known type.
     */
    public static Builder forType(Class<? extends Command> commandType) {
        return new Builder(commandType);
    }

    public abstract String getName(CommandManager manager);

    public String[] getArgs() {
        return args;
    }

    /**
     * @return true, if Bootique program should terminate, when this invocation fails
     */
    public boolean shouldTerminateOnErrors() {
        return terminateOnErrors;
    }

    static class NamedCommandWithArgs extends CommandWithArgs {
        private String commandName;

        NamedCommandWithArgs(String commandName, String[] args, boolean terminateOnErrors) {
            super(args, terminateOnErrors);
            this.commandName = commandName;
        }

        @Override
        public String getName(CommandManager manager) {
            // TODO: should we pass this through CommandManager to ensure the name is valid?
            return commandName;
        }
    }

    static class TypeCommandWithArgs extends CommandWithArgs {
        private Class<? extends Command> commandType;

        TypeCommandWithArgs(Class<? extends Command> commandType, String[] args, boolean terminateOnErrors) {
            super(args, terminateOnErrors);
            this.commandType = commandType;
        }

        @Override
        public String getName(CommandManager manager) {
            return manager.lookupByType(commandType).getMetadata().getName();
        }
    }

    /**
     * @since 0.25
     */
    public static class Builder {

        private static final String[] NO_ARGS = new String[0];

        private Class<? extends Command> commandType;
        private String commandName;

        private String[] args = NO_ARGS;
        private boolean terminateOnErrors;

        protected Builder(String commandName) {
            this.commandName = Objects.requireNonNull(commandName);
        }

        protected Builder(Class<? extends Command> commandType) {
            this.commandType = Objects.requireNonNull(commandType);
        }

        /**
         * Set command line arguments for this invocation
         */
        public Builder arguments(String[] args) {
            this.args = args != null ? args : NO_ARGS;
            return this;
        }

        /**
         * Indicate, that Bootique program should terminate, when this invocation fails.
         */
        public Builder terminateOnErrors() {
            this.terminateOnErrors = true;
            return this;
        }

        public CommandWithArgs build() {
            return commandType != null
                    ? new TypeCommandWithArgs(commandType, args, terminateOnErrors)
                    : new NamedCommandWithArgs(commandName, args, terminateOnErrors);
        }
    }
}
