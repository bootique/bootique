package io.bootique.command;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Stores a "recipe" for decorating some command with extra serial and parallel commands.
 *
 * @since 0.25
 */
public class CommandDecorator {

    private final Collection<CommandRefWithArgs> before;
    private final Collection<CommandRefWithArgs> parallel;

    private CommandDecorator() {
        this.before = new ArrayList<>(3);
        this.parallel = new ArrayList<>(3);
    }

    public static CommandDecorator.Builder builder() {
        return new Builder();
    }

    public static CommandDecorator beforeRun(Class<? extends Command> commandType, String... args) {
        return builder().beforeRun(commandType, args).build();
    }

    public static CommandDecorator beforeRun(String fullCommandName, String... commandArgs) {
        return builder().beforeRun(fullCommandName, commandArgs).build();
    }

    public static CommandDecorator alsoRun(Class<? extends Command> commandType, String... args) {
        return builder().alsoRun(commandType, args).build();
    }

    public static CommandDecorator alsoRun(String fullCommandName, String... commandArgs) {
        return builder().alsoRun(fullCommandName, commandArgs).build();
    }

    /**
     * @return Collection of hooks to run before the original command
     */
    public Collection<CommandRefWithArgs> getBefore() {
        return before;
    }

    /**
     * @return Collection of hooks to run in parallel with the original command
     */
    public Collection<CommandRefWithArgs> getParallel() {
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
         * @param fullCommandName a full un-abbreviated command name for the "also" command. The command must be known
         *                        to Bootique.
         * @param commandArgs     arguments to pass to the "before" command, including the command name.
         * @return this builder instance
         */
        public Builder beforeRun(String fullCommandName, String... commandArgs) {
            decorator.before.add(CommandRefWithArgs
                    .forName(fullCommandName)
                    .arguments(commandArgs)
                    .terminateOnErrors()
                    .build());
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
            decorator.before.add(CommandRefWithArgs.forType(commandType).arguments(args).terminateOnErrors().build());
            return this;
        }

        /**
         * Define an "also" command to run in parallel with the decorated command. The "also" Command class is deduced
         * from the passed CLI arguments. When the "also" command is run, it will receive all of the arguments passed to
         * this method as {@link io.bootique.cli.Cli} instance.
         *
         * @param fullCommandName a full un-abbreviated command name for the "also" command. The command must be known
         *                        to Bootique.
         * @param commandArgs     arguments to pass to the "before" command, including the command name.
         * @return this builder instance
         */
        public Builder alsoRun(String fullCommandName, String... commandArgs) {
            decorator.parallel.add(CommandRefWithArgs
                    .forName(fullCommandName)
                    .arguments(commandArgs)
                    .build());
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
            decorator.parallel.add(CommandRefWithArgs.forType(commandType).arguments(args).build());
            return this;
        }

        public CommandDecorator build() {
            return decorator;
        }
    }
}
