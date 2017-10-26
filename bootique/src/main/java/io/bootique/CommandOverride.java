package io.bootique;

import com.google.inject.Provider;
import io.bootique.command.Command;
import io.bootique.command.CommandInvocation;
import io.bootique.command.OverridenCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class CommandOverride {

    public static CommandOverride.Builder builder() {
        return new Builder();
    }

    private final Provider<ExecutorService> executorProvider;
    private final Collection<CommandInvocation> before;
    private final Collection<CommandInvocation> parallel;

    private CommandOverride(Provider<ExecutorService> executorProvider,
                            Collection<CommandInvocation> before,
                            Collection<CommandInvocation> parallel) {
        this.executorProvider = executorProvider;
        this.before = before;
        this.parallel = parallel;
    }

    public Command override(Command command) {
        return new OverridenCommand(command, executorProvider, before, parallel);
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

        protected CommandOverride build(Provider<ExecutorService> executorProvider) {
            return new CommandOverride(
                    executorProvider,
                    mapBuilders(before),
                    mapBuilders(parallel));
        }

        private Collection<CommandInvocation> mapBuilders(Collection<CommandInvocation.Builder> builders) {
            return (builders == null) ?
                    Collections.emptyList() :
                    builders.stream().map(CommandInvocation.Builder::build).collect(Collectors.toList());
        }
    }
}
