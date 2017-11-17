package io.bootique;

import io.bootique.command.Command;
import io.bootique.command.CommandInvocation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Stores a "recipe" for decorating some command.
 *
 * @since 0.25
 */
public class CommandDecorator {

    private final Collection<CommandInvocation> before;
    private final Collection<CommandInvocation> parallel;

    private CommandDecorator(Collection<CommandInvocation> before, Collection<CommandInvocation> parallel) {
        this.before = before;
        this.parallel = parallel;
    }

    /**
     * @since 0.25
     */
    public static CommandDecorator.Builder builder() {
        return new Builder();
    }

    /**
     * @return Collection of hooks to run before the original command
     * @since 0.25
     */
    public Collection<CommandInvocation> getBefore() {
        return before;
    }

    /**
     * @return Collection of hooks to run in parallel with the original command
     * @since 0.25
     */
    public Collection<CommandInvocation> getParallel() {
        return parallel;
    }

    /**
     * Provides a convenient DSL for building command decorators
     *
     * @since 0.25
     */
    public static class Builder {

        private Collection<CommandInvocation> before;
        private Collection<CommandInvocation> parallel;

        private Builder() {
        }

        /**
         * Add a hook to run before the original command.
         * The original command will not be run, if the hook fails.
         * Actual hook type (command) is deduced from the list of arguments.
         * <p>
         * When the hook is run, it will receive all of the provided arguments in its' own {@link io.bootique.cli.Cli} instance.
         *
         * @param args Command line arguments
         * @since 0.25
         */
        public Builder beforeRun(String... args) {
            getBefore().add(CommandInvocation.forArgs(args).terminateOnErrors().build());
            return this;
        }

        /**
         * Add a hook to run before the original command.
         * The original command will not be run, if the hook fails.
         * <p>
         * When the hook is run, it will receive all of the provided arguments in its' own {@link io.bootique.cli.Cli} instance.
         *
         * @param commandType Command to run with its' own arguments
         * @since 0.25
         */
        public Builder beforeRun(Class<? extends Command> commandType, String... args) {
            getBefore().add(CommandInvocation.forCommandType(commandType).arguments(args).terminateOnErrors().build());
            return this;
        }

        /**
         * Add a hook to run in parallel with the original command.
         * Actual hook type (command) is deduced from the list of arguments.
         * When the hook is run, it will receive all of the provided arguments in its' own {@link io.bootique.cli.Cli} instance.
         *
         * @param args Command line arguments
         * @since 0.25
         */
        public Builder alsoRun(String... args) {
            getParallel().add(CommandInvocation.forArgs(args).build());
            return this;
        }

        /**
         * Add a hook to run in parallel with the original command.
         * <p>
         * When the hook is run, it will receive all of the provided arguments in its' own {@link io.bootique.cli.Cli} instance.
         *
         * @param commandType Command to run with its' own arguments
         * @since 0.25
         */
        public Builder alsoRun(Class<? extends Command> commandType, String... args) {
            getParallel().add(CommandInvocation.forCommandType(commandType).arguments(args).build());
            return this;
        }

        private Collection<CommandInvocation> getBefore() {
            if (before == null) {
                before = new ArrayList<>();
            }
            return before;
        }

        private Collection<CommandInvocation> getParallel() {
            if (parallel == null) {
                parallel = new ArrayList<>();
            }
            return parallel;
        }

        /**
         * @since 0.25
         */
        public CommandDecorator build() {
            return new CommandDecorator(getBefore(), getParallel());
        }
    }
}
