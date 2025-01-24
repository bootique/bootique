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

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;
import io.bootique.annotation.DefaultCommand;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.Provides;
import io.bootique.di.SetBuilder;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A helper to build a non-standard command set in an app.
 */
public class Commands implements BQModule {

    private final Collection<Class<? extends Command>> commandTypes;
    private final Collection<Command> commands;
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
        if (injector.hasProvider(key)) {
            Provider<Command> commandProvider = injector.getProvider(key);
            return Optional.of(commandProvider.get());
        }
        return Optional.empty();
    }

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this).overrides(BQCoreModule.class).moduleName("Commands.Builder").build();
    }

    @Override
    public void configure(Binder binder) {
        SetBuilder<Command> extraCommandsBinder = Commands.contributeExtraCommands(binder);
        commandTypes.forEach(extraCommandsBinder::add);
        commands.forEach(extraCommandsBinder::addInstance);
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

        private final Commands commands;

        private Builder() {
            this.commands = new Commands();
        }

        public Commands module() {
            return commands;
        }

        /**
         * @deprecated in favor of {@link #module()}
         */
        @Deprecated(since = "3.0", forRemoval = true)
        public BQModuleProvider build() {
            return () -> commands;
        }

        @SafeVarargs
        public final Builder add(Class<? extends Command>... commandTypes) {
            Collections.addAll(commands.commandTypes, commandTypes);
            return this;
        }

        public final Builder add(Command... commands) {
            Collections.addAll(this.commands.commands, commands);
            return this;
        }

        public Builder noModuleCommands() {
            commands.noModuleCommands = true;
            return this;
        }
    }
}
