package com.nhl.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.launcher.command.Command;
import com.nhl.launcher.command.CommandOutcome;

public class Launcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

	private Collection<Module> modules;
	private Collection<Command> commands;

	public static Launcher bootableApp(String[] args) {
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
		CommandOutcome o = runtime.getRunner().run();

		// report error
		if (!o.isSuccess()) {

			if (o.getMessage() != null) {
				LOGGER.error(String.format("Error running command. Arguments: %s. Message: %s",
						runtime.getArgsAsString(), o.getMessage()));
			} else {
				LOGGER.error(String.format("Error running command. Arguments: %s", runtime.getArgsAsString()));
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
			commands.forEach(c -> LauncherUtil.bindCommand(binder, c));
		});

		return DIBootstrap.createInjector(finalModules);
	}
}
