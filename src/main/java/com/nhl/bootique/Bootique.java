package com.nhl.bootique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;

/**
 * A main launcher class of Bootique. To start a Bootique app, you may write
 * your main method as follows:
 * 
 * <pre>
 * public static void main(String[] args) {
 * 	Bootique.commands(_optional_commands_).modules(_optional_extensions_).run();
 * }
 * </pre>
 */
public class Bootique {

	private static final Logger LOGGER = LoggerFactory.getLogger(Bootique.class);

	private Collection<Module> modules;
	private Collection<Command> commands;

	public static Bootique app(String[] args) {
		return new Bootique().module(new BQModule(args));
	}

	private Bootique() {
		this.modules = new ArrayList<>();
		this.commands = new ArrayList<>();
	}

	public Bootique module(Module m) {
		modules.add(m);
		return this;
	}

	public Bootique modules(Module... modules) {
		Arrays.asList(modules).forEach(m -> this.modules.add(m));
		return this;
	}

	/**
	 * Registers a custom {@link Command} object.
	 */
	public Bootique command(Command command) {
		this.commands.add(command);
		return this;
	}

	/**
	 * Registers a custom {@link Command} object.
	 */
	public Bootique commands(Command... commands) {
		Arrays.asList(commands).forEach(c -> command(c));
		return this;
	}

	public void run() {

		BQRuntime runtime = new BQRuntime(createInjector());
		CommandOutcome o = runtime.run();

		// report error
		if (!o.isSuccess()) {

			if (o.getMessage() != null) {
				LOGGER.error(
						String.format("Error running command '%s': %s", runtime.getArgsAsString(), o.getMessage()));
			} else {
				LOGGER.error(String.format("Error running command '%s'", runtime.getArgsAsString()));
			}

			if (o.getException() != null) {
				LOGGER.error("Command exception", o.getException());
			}
		}

		o.exit();
	}

	private Injector createInjector() {
		Collection<Module> finalModules = new ArrayList<>(modules);
		finalModules.add((binder) -> {
			commands.forEach(c -> Multibinder.newSetBinder(binder, Command.class).addBinding().toInstance(c));
		});

		return Guice.createInjector(finalModules);
	}
}
