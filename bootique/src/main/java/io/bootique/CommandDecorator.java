package io.bootique;

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

        public Builder beforeRun(String command) {
            getBefore().add(CommandInvocation.forCommand(command).terminateOnErrors());
            return this;
        }

        /**
        public Builder beforeRun(CommandInvocation invocation) {
            ...
        }
        */

        public Builder alsoRun(String command) {
            getParallel().add(CommandInvocation.forCommand(command));
            return this;
        }

        /**
        public Builder alsoRun(CommandInvocation invocation) {
            ...
        }
        */

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
