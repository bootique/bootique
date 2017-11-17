package io.bootique;

import io.bootique.command.Command;
import io.bootique.command.CommandInvocation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Stores a "recipe" for decorating some command with extra serial and parallel commands.
 *
 * @since 0.25
 */
public class CommandDecorator {

    private final Collection<CommandInvocation> before;
    private final Collection<CommandInvocation> parallel;

    private CommandDecorator() {
        this.before = new ArrayList<>(3);
        this.parallel = new ArrayList<>(3);
    }

    public static CommandDecorator.Builder builder() {
        return new Builder();
    }

    /**
     * @return Collection of hooks to run before the original command
     */
    public Collection<CommandInvocation> getBefore() {
        return before;
    }

    /**
     * @return Collection of hooks to run in parallel with the original command
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

        private CommandDecorator decorator;

        private Builder() {
            this.decorator = new CommandDecorator();
        }

        /**
         * Define a command to run before the decorated command. The command to run in deduced from the passed CLI
         * arguments. The decorated command will not be run, if the "before" command fails.  When the "before" command is
         * run, it will receive all of the arguments passed to this method as {@link io.bootique.cli.Cli} instance.
         *
         * @param args arguments to pass to the "before" command, including the command name.
         * @return this builder instance
         */
        public Builder beforeRun(String... args) {
            decorator.before.add(CommandInvocation.forArgs(args).terminateOnErrors().build());
            return this;
        }

        /**
         * Define a command to run before the decorated command. The decorated command will not be run, if "before"
         * command fails.  When the "before" command is run, it will receive all of the arguments passed to
         * this method as {@link io.bootique.cli.Cli} instance.
         *
         * @param commandType "before" command type. Must be a command known to Bootique.
         * @param args        arguments to pass to the "before" command.
         * @return this builder instance
         */
        public Builder beforeRun(Class<? extends Command> commandType, String... args) {
            decorator.before.add(CommandInvocation.forCommandType(commandType).arguments(args).terminateOnErrors().build());
            return this;
        }

        /**
         * Define an "also" command to run in parallel with the decorated command. The "also" Command class is deduced
         * from the passed CLI arguments. When the "also" command is run, it will receive all of the arguments passed to
         * this method as {@link io.bootique.cli.Cli} instance.
         *
         * @param args arguments to pass to the "before" command, including the command name.
         * @return this builder instance
         */
        public Builder alsoRun(String... args) {
            decorator.parallel.add(CommandInvocation.forArgs(args).build());
            return this;
        }

        /**
         * Add a hook to run in parallel with the original command.
         * <p>
         * When the hook is run, it will receive all of the provided arguments in its' own {@link io.bootique.cli.Cli} instance.
         *
         * @param commandType "also" command type. Must be a command known to Bootique.
         * @param args        arguments to pass to the "also" command.
         * @return this builder instance.
         */
        public Builder alsoRun(Class<? extends Command> commandType, String... args) {
            decorator.parallel.add(CommandInvocation.forCommandType(commandType).arguments(args).build());
            return this;
        }

        public CommandDecorator build() {
            return decorator;
        }
    }
}
