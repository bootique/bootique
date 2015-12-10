package com.nhl.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.nhl.launcher.command.Command;
import com.nhl.launcher.command.CommandOutcome;

public class Launcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

	private Collection<Module> modules;
	private Collection<Command> commands;

	public static Launcher app(String[] args) {
		return new Launcher().module(new BootstrapModule(args));
	}

	private Launcher() {
		this.modules = new ArrayList<>();
		this.commands = new ArrayList<>();
	}

	public Launcher module(Module m) {
		modules.add(m);
		return this;
	}

	public Launcher modules(Module... modules) {
		Arrays.asList(modules).forEach(m -> this.modules.add(m));
		return this;
	}

	/**
	 * Registers a custom {@link Command} object.
	 */
	public Launcher command(Command command) {
		this.commands.add(command);
		return this;
	}

	/**
	 * Registers a custom {@link Command} object.
	 */
	public Launcher commands(Command... commands) {
		Arrays.asList(commands).forEach(c -> command(c));
		return this;
	}

	public void launch() {

		LauncherRuntime runtime = new LauncherRuntime(createInjector());
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
