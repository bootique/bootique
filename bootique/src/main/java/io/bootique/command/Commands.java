package io.bootique.command;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.bootique.BQCoreModule;
import io.bootique.BQModuleId;
import io.bootique.BQModuleProvider;
import io.bootique.annotation.DefaultCommand;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A helper to build a non-standard command set in an app.
 *
 * @since 0.12
 */
public class Commands implements Module {

    private Collection<Class<? extends Command>> commandTypes;
    private Collection<Command> commands;
    private boolean noModuleCommands;

    private Commands() {
        this.commandTypes = new HashSet<>();
        this.commands = new HashSet<>();
    }

    static Multibinder<Command> contributeExtraCommands(Binder binder) {
        return Multibinder.newSetBinder(binder, Command.class, ExtraCommands.class);
    }

    @SafeVarargs
    public static Builder builder(Class<? extends Command>... commandTypes) {
        return new Builder().add(commandTypes);
    }

    // copy/paste from BQCoreModule
    private static Optional<Command> defaultCommand(Injector injector) {
        // default is optional, so check via injector whether it is bound...
        Binding<Command> binding = injector.getExistingBinding(Key.get(Command.class, DefaultCommand.class));
        return binding != null ? Optional.of(binding.getProvider().get()) : Optional.empty();
    }

    @Override
    public void configure(Binder binder) {
        Multibinder<Command> extraCommandsBinder = Commands.contributeExtraCommands(binder);
        commandTypes.forEach(ct -> extraCommandsBinder.addBinding().to(ct));
        commands.forEach(c -> extraCommandsBinder.addBinding().toInstance(c));
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
                public Module module() {
                    return commands;
                }

                @Override
                public Collection<Class<? extends Module>> overrides() {
                    return Collections.singleton(BQCoreModule.class);
                }

                @Override
                public String name() {
                    return "Commands.Builder";
                }

                @Override
                public BQModuleId id() {
                    return BQModuleId.of(Commands.class);
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
