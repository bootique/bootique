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
import io.bootique.BQModuleProvider;
import io.bootique.BootiqueException;
import io.bootique.annotation.DefaultCommand;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private static Command defaultCommand(Injector injector) {
        Binding<Command> binding = injector.getExistingBinding(Key.get(Command.class, DefaultCommand.class));
        return binding != null ? binding.getProvider().get() : null;
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


        Command defaultCommand = defaultCommand(injector);

        // merge two sets, checking for dupe names within the set, but allowing
        // extras to override module commands...

        Map<String, ManagedCommand> map =
                toDistinctCommands(moduleCommands, helpCommand, defaultCommand, noModuleCommands);

        // override with logging
        toDistinctCommands(extraCommands, helpCommand, defaultCommand, false).forEach((name, command) -> {
            ManagedCommand existingCommand = map.put(name, command);
            if (existingCommand != null && existingCommand.getCommand() != command.getCommand()) {
                String i1 = existingCommand.getCommand().getClass().getName();
                String i2 = command.getCommand().getClass().getName();
                bootLogger.trace(() -> String.format("Overriding command '%s' (old command: %s, new command: %s)",
                        name, i1, i2));
            }
        });

        return new DefaultCommandManager(map);
    }

    private Map<String, ManagedCommand> toDistinctCommands(
            Set<Command> commands,
            Command helpCommand,
            Command defaultCommand,
            boolean privateCommand) {

        Map<String, ManagedCommand> commandMap = new HashMap<>();

        commands.forEach(c -> {

            String name = c.getMetadata().getName();

            ManagedCommand.Builder commandBuilder = ManagedCommand.builder(c);

            if (helpCommand == c) {
                commandBuilder.helpCommand();
            }

            if (defaultCommand != null && defaultCommand == c) {
                commandBuilder.defaultCommand();
            }

            if (privateCommand) {
                commandBuilder.privateCommand();
            }

            ManagedCommand existing = commandMap.put(name, commandBuilder.build());

            // complain on dupes
            if (existing != null && existing.getCommand() != c) {
                String c1 = existing.getCommand().getClass().getName();
                String c2 = c.getClass().getName();

                String message = String.format("More than one DI command named '%s'. Conflicting types: %s, %s.",
                        name, c1, c2);
                throw new BootiqueException(1, message);
            }
        });

        return commandMap;
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
            };
        }

        @SafeVarargs
        public final Builder add(Class<? extends Command>... commandTypes) {
            for (Class<? extends Command> ct : commandTypes) {
                commands.commandTypes.add(ct);
            }

            return this;
        }

        @SafeVarargs
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
