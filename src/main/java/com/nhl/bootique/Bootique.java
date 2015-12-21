package com.nhl.bootique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.ServiceLoader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.env.DefaultEnvironment;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;

/**
 * A main launcher class of Bootique. To start a Bootique app, you may write
 * your main method as follows:
 * 
 * <pre>
 * public static void main(String[] args) {
 * 	Bootique.app(args)commands(_optional_commands_).modules(_optional_extensions_).run();
 * }
 * </pre>
 * 
 * or
 * 
 * <pre>
 * public static void main(String[] args) {
 * 	Bootique.app(args).commands(_optional_commands_).autoLoadModules().run();
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

	protected Collection<BQModuleProvider> providers;
	private Collection<Command> commands;
	private String[] args;
	private boolean autoLoadModules;
	private BootLogger bootLogger;

	public static Bootique app(String[] args) {
		return new Bootique(args);
	}

	private Bootique(String[] args) {
		this.args = args;
		this.providers = new ArrayList<>();
		this.commands = new ArrayList<>();
		this.autoLoadModules = false;
		this.bootLogger = createBootLogger();
	}

	/**
	 * Instructs Bootique to load any modules available on class-path that
	 * expose {@link BQModuleProvider} provider. Auto-loaded modules will be
	 * used in default configuration. Factories within modules will of course be
	 * configured dynamically from YAML.
	 * <p>
	 * <i>Use with caution, you may load more modules than you expected. Make
	 * sure only needed Bootique dependencies are included on class-path. If in
	 * doubt, switch to explicit Module loading via
	 * {@link #modules(Class...)}</i>
	 * 
	 * @see BQModuleProvider
	 */
	public Bootique autoLoadModules() {
		this.autoLoadModules = true;
		return this;
	}

	/**
	 * @since 0.8
	 */
	public Bootique module(Class<? extends Module> moduleType) {
		Objects.requireNonNull(moduleType);
		providers.add(() -> createModule(moduleType));
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
		Objects.requireNonNull(m);
		providers.add(() -> m);
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

	protected BootLogger createBootLogger() {
		return new DefaultBootLogger(System.getProperty(DefaultEnvironment.TRACE_PROPERTY) != null);
	}

	protected Injector createInjector() {

		Collection<BQModuleProvider> providers = new ArrayList<>();
		providers.add(coreModuleProvider(args, bootLogger));

		if (!commands.isEmpty()) {
			providers.add(commandsProvider());
		}

		providers.addAll(builderProviders());

		if (autoLoadModules) {
			providers.addAll(autoLoadedProviders());
		}

		Collection<Module> modules = new ModuleMerger(bootLogger).getModules(providers);
		return Guice.createInjector(modules);
	}

	protected BQModuleProvider commandsProvider() {

		return () -> {
			bootLogger.trace(() -> "Adding module with custom commands...");
			return (b) -> BQBinder.contributeTo(b).commands(commands);
		};
	}

	protected Collection<BQModuleProvider> builderProviders() {
		return providers;
	}

	protected BQModuleProvider coreModuleProvider(String[] args, BootLogger bootLogger) {
		return () -> new BQCoreModule(args, bootLogger);
	}

	protected Collection<BQModuleProvider> autoLoadedProviders() {
		Collection<BQModuleProvider> modules = new ArrayList<>();
		ServiceLoader.load(BQModuleProvider.class).forEach(p -> modules.add(p));
		return modules;
	}
}
