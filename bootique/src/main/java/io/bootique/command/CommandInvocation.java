package io.bootique.command;

import java.util.Optional;

/**
 * Contains a "recipe" for invoking a command.
 *
 * @since 0.25
 */
public class CommandInvocation {

    /**
     * Start building an invocation with a list of arguments.
     *
     * @param args Command line arguments
     */
    public static Builder forArgs(String[] args) {
        return new Builder().arguments(args);
    }

    /**
     * Start building an invocation of an explicit command.
     */
    public static Builder forCommandType(Class<? extends Command> commandType) {
        return new Builder(commandType);
    }

    private final Optional<Class<? extends Command>> commandType;
    private final String[] args;
    private final boolean terminateOnErrors;

    private CommandInvocation(Class<? extends Command> commandType,
                              String[] args,
                              boolean terminateOnErrors) {
        this.commandType = Optional.ofNullable(commandType);
        this.args = args;
        this.terminateOnErrors = terminateOnErrors;
    }

    /**
     * Returns a command type, or an {@link Optional#empty()},
     * if this invocation is based on command line arguments.
     *
     * @return Command type, if present
     */
    public Optional<Class<? extends Command>> getCommandType() {
        return commandType;
    }

    /**
     * @return Command line arguments (may be empty)
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * @return true, if Bootique program should terminate, when this invocation fails
     */
    public boolean shouldTerminateOnErrors() {
        return terminateOnErrors;
    }

    /**
     * @since 0.25
     */
    public static class Builder {
        private static final String[] NO_ARGS = new String[0];

        private Class<? extends Command> commandType;
        private String[] args;
        private boolean terminateOnErrors;

        private Builder() {
            this(null);
        }

        private Builder(Class<? extends Command> commandType) {
            this.commandType = commandType;
            this.args = NO_ARGS;
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
        
        public CommandInvocation build() {
            return new CommandInvocation(commandType, args, terminateOnErrors);
        }
    }
}
