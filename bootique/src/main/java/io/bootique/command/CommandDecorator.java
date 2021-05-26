/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.command;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A recipe for decorating some unspecified command with extra serial and parallel commands.
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

    public static CommandDecorator beforeRun(Command command) {
        return builder().beforeRun(command).build();
    }

    public static CommandDecorator beforeRun(Class<? extends Command> commandType, String... args) {
        return builder().beforeRun(commandType, args).build();
    }

    public static CommandDecorator beforeRun(String fullCommandName, String... commandArgs) {
        return builder().beforeRun(fullCommandName, commandArgs).build();
    }

    public static CommandDecorator alsoRun(Command command) {
        return builder().alsoRun(command).build();
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
                    .nameRef(fullCommandName)
                    .arguments(commandArgs)
                    .terminateOnErrors()
                    .build());
            return this;
        }

        public Builder copyFrom(CommandDecorator decorator) {
            decorator.getBefore().forEach(this.decorator.before::add);
            decorator.getParallel().forEach(this.decorator.parallel::add);
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
            decorator.before.add(CommandRefWithArgs.typeRef(commandType).arguments(args).terminateOnErrors().build());
            return this;
        }

        public Builder beforeRun(Command command) {
            decorator.before.add(CommandRefWithArgs.commandRef(command).terminateOnErrors().build());
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
                    .nameRef(fullCommandName)
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
            decorator.parallel.add(CommandRefWithArgs.typeRef(commandType).arguments(args).build());
            return this;
        }

        public Builder alsoRun(Command command) {
            decorator.parallel.add(CommandRefWithArgs.commandRef(command).build());
            return this;
        }

        public CommandDecorator build() {
            return decorator;
        }
    }
}
