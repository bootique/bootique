package com.nhl.bootique.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQModuleProvider;
import com.nhl.bootique.annotation.DefaultCommand;

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
	CommandManager createManager(Set<Command> moduleCommands, @ExtraCommands Set<Command> extraCommands,
			@DefaultCommand Command defaultCommand) {

		Set<Command> combinedCommands = new HashSet<>(extraCommands);

		if (!noModuleCommands) {

			// TODO: override similarly named module commands with extra
			// commands...
			combinedCommands.addAll(moduleCommands);
		}

		return new DefaultCommandManager(combinedCommands, defaultCommand);
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
