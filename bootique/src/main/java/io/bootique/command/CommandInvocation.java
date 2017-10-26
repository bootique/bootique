package io.bootique.command;

import java.util.Objects;

public class CommandInvocation {

    public static Builder forCommand(String command) {
        return new Builder(command);
    }

    /**
    public static Builder forRunnable(Runnable runnable) {
        ...
    }
    */

    private final String command;
    private final boolean terminateOnErrors;

    private CommandInvocation(String command, boolean terminateOnErrors) {
        this.command = command;
        this.terminateOnErrors = terminateOnErrors;
    }

    public String getCommand() {
        return command;
    }

    public boolean terminateOnErrors() {
        return terminateOnErrors;
    }

    public static class Builder {

        private final String command;
        private boolean terminateOnErrors;

        private Builder(String command) {
            this.command = Objects.requireNonNull(command, "Missing command");
        }

        public Builder terminateOnErrors() {
            this.terminateOnErrors = true;
            return this;
        }

        public CommandInvocation build() {
            return new CommandInvocation(command, terminateOnErrors);
        }
    }
}
