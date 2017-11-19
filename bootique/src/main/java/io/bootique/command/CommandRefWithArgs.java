package io.bootique.command;

import java.util.Objects;

/**
 * Stores a reference to a command and a set of invocation arguments. Used to capture auxiliary commands to be run with
 * the main command.
 *
 * @since 0.25
 */
public abstract class CommandRefWithArgs {

    private final String[] args;
    private final boolean terminateOnErrors;

    protected CommandRefWithArgs(String[] args, boolean terminateOnErrors) {
        this.args = args;
        this.terminateOnErrors = terminateOnErrors;
    }

    /**
     * Starts building an invocation with a named command.
     *
     * @param fullCommandName full name of the command.
     */
    public static ArgsBuilder nameRef(String fullCommandName) {
        return new ArgsBuilder(fullCommandName);
    }

    /**
     * Starts building an invocation with a command of a known type.
     */
    public static ArgsBuilder typeRef(Class<? extends Command> commandType) {
        return new ArgsBuilder(commandType);
    }

    /**
     * Starts building an invocation with a command of a known type.
     */
    public static NoArgsBuilder commandRef(Command command) {
        return new NoArgsBuilder(command);
    }

    public abstract Command resolve(CommandManager manager);

    public String[] getArgs() {
        return args;
    }

    /**
     * @return true, if Bootique program should terminate, when this invocation fails
     */
    public boolean shouldTerminateOnErrors() {
        return terminateOnErrors;
    }

    static class RefByNameWithArgs extends CommandRefWithArgs {
        private String commandName;

        RefByNameWithArgs(String commandName, String[] args, boolean terminateOnErrors) {
            super(args, terminateOnErrors);
            this.commandName = commandName;
        }

        @Override
        public Command resolve(CommandManager manager) {
            return manager.lookupByName(commandName).getCommand();
        }
    }

    static class RefByTypeWithArgs extends CommandRefWithArgs {
        private Class<? extends Command> commandType;

        RefByTypeWithArgs(Class<? extends Command> commandType, String[] args, boolean terminateOnErrors) {
            super(args, terminateOnErrors);
            this.commandType = commandType;
        }

        @Override
        public Command resolve(CommandManager manager) {
            return manager.lookupByType(commandType).getCommand();
        }
    }

    static class RefByInstanceWithArgs extends CommandRefWithArgs {
        private Command command;

        RefByInstanceWithArgs(Command command, boolean terminateOnErrors) {
            super(ArgsBuilder.NO_ARGS, terminateOnErrors);
            this.command = command;
        }

        @Override
        public Command resolve(CommandManager manager) {
            return command;
        }
    }

    /**
     * A builder of {@link CommandRefWithArgs} bound to a specific command. In this case the command doesn't have to be
     * registered in Bootique, and hence we can't parse the arguments. So any parameters need to be captured within the
     * command itself.
     *
     * @since 0.25
     */
    public static class NoArgsBuilder<T extends NoArgsBuilder<T>> {

        private Command command;
        private boolean terminateOnErrors;

        protected NoArgsBuilder(Command command) {
            this.command = Objects.requireNonNull(command);
        }

        /**
         * Indicate, that Bootique program should terminate, when this invocation fails.
         */
        public NoArgsBuilder<T> terminateOnErrors() {
            this.terminateOnErrors = true;
            return this;
        }

        public CommandRefWithArgs build() {
            return new RefByInstanceWithArgs(command, terminateOnErrors);
        }
    }

    /**
     * @since 0.25
     */
    public static class ArgsBuilder {

        private static final String[] NO_ARGS = new String[0];

        private String commandName;
        private Class<? extends Command> commandType;

        private String[] args = NO_ARGS;
        private boolean terminateOnErrors;

        protected ArgsBuilder(String commandName) {
            this.commandName = Objects.requireNonNull(commandName);
        }

        protected ArgsBuilder(Class<? extends Command> commandType) {
            this.commandType = Objects.requireNonNull(commandType);
        }

        /**
         * Set command line arguments for this invocation
         */
        public ArgsBuilder arguments(String[] args) {
            this.args = args != null ? args : NO_ARGS;
            return this;
        }

        /**
         * Indicate, that Bootique program should terminate, when this invocation fails.
         */
        public ArgsBuilder terminateOnErrors() {
            this.terminateOnErrors = true;
            return this;
        }

        public CommandRefWithArgs build() {
            return (commandType != null) ?
                    new RefByTypeWithArgs(commandType, args, terminateOnErrors)
                    : new RefByNameWithArgs(commandName, args, terminateOnErrors);
        }
    }
}
