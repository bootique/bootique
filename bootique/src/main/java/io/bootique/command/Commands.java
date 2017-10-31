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
import io.bootique.CommandDecorator;
import io.bootique.annotation.DefaultCommand;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

	static Multibinder<Command> contributeExtraCommands(Binder binder) {
		return Multibinder.newSetBinder(binder, Command.class, ExtraCommands.class);
	}

	@SafeVarargs
	public static Builder builder(Class<? extends Command>... commandTypes) {
		return new Builder().add(commandTypes);
	}

	private Commands() {
		this.commandTypes = new HashSet<>();
		this.commands = new HashSet<>();
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
								 Map<String, CommandDecorator> commandDecorators,
                                 HelpCommand helpCommand,
								 Injector injector,
								 BootLogger bootLogger) {

		// merge two sets, checking for dupe names within the set, but allowing
		// extras to override module commands...

		Map<String, Command> map;

		if (noModuleCommands) {
			map = toDistinctCommands(extraCommands);
		} else {
			map = toDistinctCommands(moduleCommands);

			// override with logging
			toDistinctCommands(extraCommands).forEach((name, command) -> {
				Command existingCommand = map.put(name, command);
				if (existingCommand != null && existingCommand != command) {
					String i1 = existingCommand.getClass().getName();
					String i2 = command.getClass().getName();
					bootLogger.trace(() -> String.format("Overriding command '%s' (old command: %s, new command: %s)",
							name, i1, i2));
				}
			});
		}

		// copy/paste from BQCoreModule
		Binding<Command> binding = injector.getExistingBinding(Key.get(Command.class, DefaultCommand.class));
		Command defaultCommand = binding != null ? binding.getProvider().get() : null;

		commandDecorators.forEach((commandName, commandDecorator) -> {
            Command originalCommand = map.get(commandName);
            if (originalCommand == null) {
                throw new BootiqueException(1, "Attempted to decorate an unknown command: " + commandName);
            }
            map.put(commandName, commandDecorator.decorate(originalCommand));
        });

		return new DefaultCommandManager(map, Optional.ofNullable(defaultCommand), Optional.of(helpCommand));
	}

	private Map<String, Command> toDistinctCommands(Set<Command> commands) {
		Map<String, Command> commandMap = new HashMap<>();

		commands.forEach(c -> {

			String name = c.getMetadata().getName();
			Command existing = commandMap.put(name, c);

			// complain about dupes
			if (existing != null && existing != c) {
				String c1 = existing.getClass().getName();
				String c2 = c.getClass().getName();
				throw new RuntimeException(
						String.format("Duplicate command for name %s (provided by: %s and %s) ", name, c1, c2));
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
