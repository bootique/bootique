package io.bootique.command;

import java.util.Objects;
import java.util.Optional;

public class CommandInvocation {

    public static Builder forArgs(String[] args) {
        return new Builder().arguments(args);
    }

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

    public Optional<Class<? extends Command>> getCommandType() {
        return commandType;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean shouldTerminateOnErrors() {
        return terminateOnErrors;
    }

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

        public Builder arguments(String[] args) {
            this.args = Objects.requireNonNull(args, "Missing arguments");
            return this;
        }

        public Builder terminateOnErrors() {
            this.terminateOnErrors = true;
            return this;
        }

        public CommandInvocation build() {
            return new CommandInvocation(commandType, args, terminateOnErrors);
        }
    }
}
