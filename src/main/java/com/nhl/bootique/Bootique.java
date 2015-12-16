package com.nhl.bootique;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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

	protected static Module createModule(Class<? extends Module> moduleType) {
		try {
			return moduleType.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error instantiating Module of type: " + moduleType.getName(), e);
		}
	}

	protected Collection<Module> modules;
	protected Collection<Class<? extends Module>> moduleTypes;
	private Collection<Command> commands;
	private String[] args;

	public static Bootique app(String[] args) {
		return new Bootique(args);
	}

	private Bootique(String[] args) {
		this.args = args;
		this.modules = new ArrayList<>();
		this.moduleTypes = new HashSet<>();
		this.commands = new ArrayList<>();
	}

	/**
	 * @since 0.8
	 */
	public Bootique module(Class<? extends Module> moduleType) {
		Preconditions.checkNotNull(moduleType);
		moduleTypes.add(moduleType);
		return this;
	}

	/**
	 * @since 0.8
	 */
	@SafeVarargs
	public final Bootique modules(Class<? extends Module>... moduleTypes) {
		Arrays.asList(moduleTypes).forEach(m -> module(m));
		return this;
	}

	public Bootique module(Module m) {
		Preconditions.checkNotNull(m);
		modules.add(m);
		return this;
	}

	public Bootique modules(Module... modules) {
		Arrays.asList(modules).forEach(m -> module(m));
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
				runtime.getBootLogger().stderr(
						String.format("Error running command '%s': %s", runtime.getArgsAsString(), o.getMessage()));
			} else {
				runtime.getBootLogger().stderr(String.format("Error running command '%s'", runtime.getArgsAsString()));
			}

			if (o.getException() != null) {
				runtime.getBootLogger().stderr("Command exception", o.getException());
			}
		}

		o.exit();
	}

	protected Injector createInjector() {
		Collection<Module> finalModules = new ArrayList<>();

		finalModules.add(createCoreModule(args));
		finalModules.addAll(modules);
		finalModules.addAll(moduleTypes.stream().map(mt -> createModule(mt)).collect(toList()));
		finalModules.add((b) -> BQContribBinder.binder(b).bindCommands(commands));

		return Guice.createInjector(finalModules);
	}

	protected Module createCoreModule(String[] args) {
		return new BQCoreModule(args);
	}
}
