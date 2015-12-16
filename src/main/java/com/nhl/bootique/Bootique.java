package com.nhl.bootique;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

	protected Map<Class<? extends BQBundle>, String> bundleTypes;
	protected Collection<Module> modules;
	private Collection<Command> commands;
	private String[] args;

	public static Bootique app(String[] args) {
		return new Bootique(args);
	}

	protected static Module createBundleModule(Class<? extends BQBundle> bundleType, String configPrefix) {
		BQBundle bundle;
		try {
			bundle = bundleType.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error instantiating bundle of type: " + bundleType.getName(), e);
		}

		return configPrefix != null ? bundle.module(configPrefix) : bundle.module();
	}

	private Bootique(String[] args) {
		this.args = args;
		this.bundleTypes = new HashMap<>();
		this.modules = new ArrayList<>();
		this.commands = new ArrayList<>();
	}

	/**
	 * @since 0.8
	 */
	@SafeVarargs
	public final Bootique bundles(Class<? extends BQBundle>... bundleTypes) {
		Preconditions.checkNotNull(bundleTypes);
		Arrays.asList(bundleTypes).forEach(bt -> bundle(bt));
		return this;
	}

	/**
	 * @since 0.8
	 */
	public Bootique bundle(Class<? extends BQBundle> bundleType) {
		return bundle(bundleType, null);
	}

	/**
	 * @since 0.8
	 */
	public Bootique bundle(Class<? extends BQBundle> bundleType, String configPrefix) {
		Preconditions.checkNotNull(bundleType);
		bundleTypes.put(bundleType, configPrefix);
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
		Collection<Module> finalModules = new ArrayList<>(modules.size() + 3);

		finalModules.add(createCoreModule(args));
		finalModules.addAll(createBundleModule());
		finalModules.addAll(modules);
		finalModules.add((binder) -> BQModule.bindCommands(binder, commands));

		return Guice.createInjector(finalModules);
	}

	protected Collection<Module> createBundleModule() {
		return bundleTypes.entrySet().stream().map(e -> createBundleModule(e.getKey(), e.getValue())).collect(toList());
	}

	protected Module createCoreModule(String[] args) {
		return new BQModule(args);
	}
}
