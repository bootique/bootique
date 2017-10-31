package io.bootique;

import io.bootique.command.Command;
import io.bootique.command.CommandInvocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class CommandDecorator {

    public static CommandDecorator.Builder builder() {
        return new Builder();
    }

    private final Collection<CommandInvocation> before;
    private final Collection<CommandInvocation> parallel;

    private CommandDecorator(Collection<CommandInvocation> before,
                             Collection<CommandInvocation> parallel) {
        this.before = before;
        this.parallel = parallel;
    }

    public Collection<CommandInvocation> getBefore() {
        return before;
    }

    public Collection<CommandInvocation> getParallel() {
        return parallel;
    }

    public static class Builder {

        private Collection<CommandInvocation.Builder> before;
        private Collection<CommandInvocation.Builder> parallel;

        private Builder() {
        }

        public Builder beforeRun(String[] args) {
            getBefore().add(CommandInvocation.forArgs(args).terminateOnErrors());
            return this;
        }

        public Builder beforeRun(Class<? extends Command> commandType) {
            getBefore().add(CommandInvocation.forCommandType(commandType).terminateOnErrors());
            return this;
        }

        public Builder beforeRun(Class<? extends Command> commandType, String[] args) {
            getBefore().add(CommandInvocation.forCommandType(commandType).arguments(args).terminateOnErrors());
            return this;
        }

        public Builder alsoRun(String[] args) {
            getParallel().add(CommandInvocation.forArgs(args));
            return this;
        }

        public Builder alsoRun(Class<? extends Command> commandType) {
            getParallel().add(CommandInvocation.forCommandType(commandType));
            return this;
        }

        public Builder alsoRun(Class<? extends Command> commandType, String[] args) {
            getParallel().add(CommandInvocation.forCommandType(commandType).arguments(args));
            return this;
        }

        private Collection<CommandInvocation.Builder> getBefore() {
            if (before == null) {
                before = new ArrayList<>();
            }
            return before;
        }

        private Collection<CommandInvocation.Builder> getParallel() {
            if (parallel == null) {
                parallel = new ArrayList<>();
            }
            return parallel;
        }

        protected CommandDecorator build() {
            return new CommandDecorator(mapBuilders(before), mapBuilders(parallel));
        }

        private Collection<CommandInvocation> mapBuilders(Collection<CommandInvocation.Builder> builders) {
            return (builders == null) ?
                    Collections.emptyList() :
                    builders.stream().map(CommandInvocation.Builder::build).collect(Collectors.toList());
        }
    }
}
