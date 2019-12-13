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

import io.bootique.annotation.DefaultCommand;
import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.BQModule;
import io.bootique.di.Provides;
import io.bootique.di.SetBuilder;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * A helper to build a non-standard command set in an app.
 *
 * @since 0.12
 */
public class Commands implements BQModule {

    private Collection<Class<? extends Command>> commandTypes;
    private Collection<Command> commands;
    private boolean noModuleCommands;

    private Commands() {
        this.commandTypes = new HashSet<>();
        this.commands = new HashSet<>();
    }

    static SetBuilder<Command> contributeExtraCommands(Binder binder) {
        return binder.bindSet(Command.class, ExtraCommands.class);
    }

    @SafeVarargs
    public static Builder builder(Class<? extends Command>... commandTypes) {
        return new Builder().add(commandTypes);
    }

    // copy/paste from BQCoreModule
    private static Optional<Command> defaultCommand(Injector injector) {
        // default is optional, so check via injector whether it is bound...
        Key<Command> key = Key.get(Command.class, DefaultCommand.class);
        if(injector.hasProvider(key)) {
            Provider<Command> commandProvider = injector.getProvider(key);
            return Optional.of(commandProvider.get());
        }
        return Optional.empty();
    }

    @Override
    public void configure(Binder binder) {
        SetBuilder<Command> extraCommandsBinder = Commands.contributeExtraCommands(binder);
        commandTypes.forEach(extraCommandsBinder::add);
        commands.forEach(extraCommandsBinder::add);
    }

    @Provides
    @Singleton
    CommandManager createManager(Set<Command> moduleCommands,
                                 @ExtraCommands Set<Command> extraCommands,
                                 HelpCommand helpCommand,
                                 Injector injector,
                                 BootLogger bootLogger) {

        return new CommandManagerWithOverridesBuilder(moduleCommands, bootLogger)
                .defaultCommand(defaultCommand(injector))
                .helpCommand(helpCommand)
                .hideBaseCommands(noModuleCommands)
                .overrideWith(extraCommands)
                .build();
    }

    public static class Builder {

        private Commands commands;

        private Builder() {
            this.commands = new Commands();
        }

        public BQModuleProvider build() {

            return new BQModuleProvider() {

                @Override
                public BQModule module() {
                    return commands;
                }

                @Override
                public Collection<Class<? extends BQModule>> overrides() {
                    return Collections.singleton(BQCoreModule.class);
                }

                @Override
                public String name() {
                    return "Commands.Builder";
                }
            };
        }

        @SafeVarargs
        public final Builder add(Class<? extends Command>... commandTypes) {
            for (Class<? extends Command> ct : commandTypes) {
                commands.commandTypes.add(ct);
            }

            return this;
        }

        public final Builder add(Command... commands) {
            for (Command c : commands) {
                this.commands.commands.add(c);
            }

            return this;
        }

        public Builder noModuleCommands() {
            commands.noModuleCommands = true;
            return this;
        }
    }
}
