package com.nhl.bootique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;

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
	private boolean autoLoadExtensions;
	private BootLogger bootLogger;

	public static Bootique app(String[] args) {
		return new Bootique(args);
	}

	private Bootique(String[] args) {
		this.args = args;
		this.modules = new ArrayList<>();
		this.moduleTypes = new HashSet<>();
		this.commands = new ArrayList<>();
		this.autoLoadExtensions = false;
		this.bootLogger = new DefaultBootLogger();
	}

	/**
	 * Instructs Bootique to load any {@link BQModuleProvider} providers
	 * available on class-path using Java ServiceLoader mechanism. Note that
	 * auto-loaded modules will be used in default configuration. Factories
	 * within modules will of course be configured dynamically from YAML.
	 * 
	 * @see BQModuleProvider
	 */
	public Bootique autoLoadExtensions() {
		this.autoLoadExtensions = true;
		return this;
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

		finalModules.add(createCoreModule(args, bootLogger));
		finalModules.addAll(createBuilderModules());
		finalModules.addAll(createAutoLoadedExtensions());
		finalModules.addAll(createCommandsModules());

		return Guice.createInjector(finalModules);
	}

	protected Collection<Module> createCommandsModules() {
		if (commands.isEmpty()) {
			return Collections.emptyList();
		}

		bootLogger.stdout("Adding module with custom commands...");
		Module m = (b) -> BQContribBinder.binder(b).bindCommands(commands);
		return Collections.singletonList(m);
	}

	protected Collection<Module> createBuilderModules() {
		Collection<Module> modules = new ArrayList<>();

		this.modules.forEach(m -> {
			bootLogger.stdout(String.format("Adding module '%s'...", m.getClass().getName()));
			modules.add(m);
		});

		this.moduleTypes.forEach(mt -> {
			Module m = createModule(mt);
			bootLogger.stdout(String.format("Adding module '%s'...", m.getClass().getName()));
			modules.add(m);
		});

		return modules;
	}

	protected Module createCoreModule(String[] args, BootLogger bootLogger) {
		bootLogger.stdout(String.format("Adding module '%s' (core)...", BQCoreModule.class.getName()));
		return new BQCoreModule(args, bootLogger);
	}

	protected Collection<Module> createAutoLoadedExtensions() {
		if (!autoLoadExtensions) {
			return Collections.emptySet();
		}

		Collection<Module> modules = new ArrayList<>();
		ServiceLoader.load(BQModuleProvider.class).forEach(p -> {
			Module m = p.module();
			modules.add(m);

			bootLogger.stdout(String.format("Adding module '%s' provided by '%s' (auto-loaded)...",
					m.getClass().getName(), p.getClass().getName()));
		});
		return modules;
	}
}
