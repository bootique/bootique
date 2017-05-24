package io.bootique;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import io.bootique.command.CommandOutcome;
import io.bootique.env.DefaultEnvironment;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.shutdown.DefaultShutdownManager;
import io.bootique.shutdown.ShutdownManager;
import joptsimple.OptionException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

/**
 * A main launcher class of Bootique. To start a Bootique app, you may write your main method as follows:
 * <pre>
 * public static void main(String[] args) {
 * 	  Bootique.app(args).modules(...).exec().exit();
 * }
 * </pre>
 * or
 * <pre>
 * public static void main(String[] args) {
 * 	  Bootique.app(args).autoLoadModules().exec().exit();
 * }
 * </pre>
 * or just use this class as the main app class.
 */
public class Bootique {

    protected Collection<BQModuleProvider> providers;
    private String[] args;
    private boolean autoLoadModules;
    private BootLogger bootLogger;
    private ShutdownManager shutdownManager;

    private Bootique(String[] args) {
        this.args = args;
        this.providers = new ArrayList<>();
        this.autoLoadModules = false;
        this.bootLogger = createBootLogger();
        this.shutdownManager = createShutdownManager();
    }

    protected static Module createModule(Class<? extends Module> moduleType) {
        try {
            return moduleType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error instantiating Module of type: " + moduleType.getName(), e);
        }
    }

    static String[] mergeArrays(String[] a1, String[] a2) {
        if (a1.length == 0) {
            return a2;
        }

        if (a2.length == 0) {
            return a1;
        }

        String[] merged = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, merged, 0, a1.length);
        System.arraycopy(a2, 0, merged, a1.length, a2.length);

        return merged;
    }

    static String[] toArray(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }

    /**
     * A generic main method that auto-loads available modules and runs Bootique stack. Useful for apps that don't
     * care to customize their "main()".
     *
     * @param args app arguments passed by the shell.
     * @since 0.17
     */
    public static void main(String[] args) {
        Bootique.app(args).autoLoadModules().exec().exit();
    }

    /**
     * Starts a builder of Bootique runtime.
     *
     * @param args command-line arguments.
     * @return Bootique object that can be customized and then executed as an
     * app via {@link #exec()} method.
     */
    public static Bootique app(String... args) {
        if (args == null) {
            args = new String[0];
        }

        return new Bootique(args);
    }

    /**
     * Starts a builder of Bootique runtime, initializing it with provided
     * command-line arguments.
     *
     * @param args command-line arguments.
     * @return Bootique object that can be customized and then executed as an
     * app via {@link #exec()} method.
     * @since 0.17
     */
    public static Bootique app(Collection<String> args) {
        if (args == null) {
            args = Collections.emptyList();
        }

        return app(toArray(Objects.requireNonNull(args)));
    }

    /**
     * Optionally overrides Bootique's BootLogger.
     *
     * @param bootLogger a custom BootLogger. Has to be non-null.
     * @return this instance of Bootique.
     * @since 0.12
     */
    public Bootique bootLogger(BootLogger bootLogger) {
        this.bootLogger = Objects.requireNonNull(bootLogger);
        return this;
    }

    /**
     * Optionally overrides Bootique's ShutdownManager.
     *
     * @param shutdownManager a custom {@link ShutdownManager} to use in this execution of Bootique. Has to be non-null.
     * @return this instance of Bootique.
     * @since 0.23
     */
    public Bootique shutdownManager(ShutdownManager shutdownManager) {
        this.shutdownManager = Objects.requireNonNull(shutdownManager);
        return this;
    }

    /**
     * Appends extra values to Bootique CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of Bootique
     * @since 0.17
     */
    public Bootique args(String... args) {
        if (args != null) {
            this.args = Bootique.mergeArrays(this.args, args);
        }
        return this;
    }

    /**
     * Appends extra values to Bootique CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of Bootique
     * @since 0.17
     */
    public Bootique args(Collection<String> args) {
        if (args != null) {
            this.args = Bootique.mergeArrays(this.args, Bootique.toArray(args));
        }
        return this;
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
     * {@link #modules(Class...)}</i>.
     *
     * @return this Bootique instance
     * @see BQModuleProvider
     */
    public Bootique autoLoadModules() {
        this.autoLoadModules = true;
        return this;
    }

    /**
     * @param moduleType custom Module class to add to Bootique DI runtime.
     * @return this Bootique instance
     * @see #autoLoadModules()
     * @since 0.8
     */
    public Bootique module(Class<? extends Module> moduleType) {
        Objects.requireNonNull(moduleType);
        providers.add(() -> createModule(moduleType));
        return this;
    }

    /**
     * Adds an array of Module types to the Bootique DI runtime. Each type will
     * be instantiated by Bootique and added to the Guice DI container.
     *
     * @param moduleTypes custom Module classes to add to Bootique DI runtime.
     * @return this Bootique instance
     * @see #autoLoadModules()
     * @since 0.8
     */
    @SafeVarargs
    public final Bootique modules(Class<? extends Module>... moduleTypes) {
        Arrays.asList(moduleTypes).forEach(m -> module(m));
        return this;
    }

    public Bootique module(Module m) {
        Objects.requireNonNull(m);
        providers.add(new BQModuleProvider() {

            @Override
            public Module module() {
                return m;
            }

            @Override
            public String name() {
                return "Bootique";
            }
        });
        return this;
    }

    /**
     * Adds an array of Modules to the Bootique DI runtime.
     *
     * @param modules an array of modules to add to Bootiqie DI runtime.
     * @return this instance of {@link Bootique}.
     */
    public Bootique modules(Module... modules) {
        Arrays.asList(modules).forEach(m -> module(m));
        return this;
    }

    /**
     * Adds a Module generated by the provider. Provider may optionally specify
     * that the Module overrides services in some other Module.
     *
     * @param moduleProvider a provider of Module and override spec.
     * @return this instance of {@link Bootique}.
     * @since 0.12
     */
    public Bootique module(BQModuleProvider moduleProvider) {
        Objects.requireNonNull(moduleProvider);
        providers.add(moduleProvider);
        return this;
    }

    /**
     * Starts an API call chain to override an array of Modules.
     *
     * @param overriddenTypes an array of modules whose bindings should be overridden.
     * @return {@link BQModuleOverrideBuilder} object to specify a Module
     * overriding other modules.
     */
    @SafeVarargs
    public final BQModuleOverrideBuilder<Bootique> override(Class<? extends Module>... overriddenTypes) {
        return new BQModuleOverrideBuilder<Bootique>() {

            @Override
            public Bootique with(Class<? extends Module> moduleType) {

                providers.add(new BQModuleProvider() {

                    @Override
                    public Module module() {
                        return createModule(moduleType);
                    }

                    @Override
                    public Collection<Class<? extends Module>> overrides() {
                        return Arrays.asList(overriddenTypes);
                    }
                });

                return Bootique.this;
            }

            @Override
            public Bootique with(Module module) {
                providers.add(new BQModuleProvider() {

                    @Override
                    public Module module() {
                        return module;
                    }

                    @Override
                    public Collection<Class<? extends Module>> overrides() {
                        return Arrays.asList(overriddenTypes);
                    }
                });

                return Bootique.this;
            }
        };
    }

    /**
     * Creates and returns an instance of {@link BQRuntime} that contains all Bootique services, commands, etc.
     * This method is only needed if you are managing Bootique execution sequence manually. Normally you'd be using
     * {@link #exec()} method instead of this method.
     *
     * @return a new {@link BQRuntime} instance that contains all Bootique services, commands, etc.
     * @since 0.13
     */
    public BQRuntime createRuntime() {
        Injector injector = createInjector();
        return createRuntime(injector);
    }

    /**
     * Executes Bootique application, exiting the JVM at the end.
     * <p>
     * If you don't want your app to shutdown after executing Bootique, call {@link #exec()} instead.
     *
     * @deprecated since 0.23 in favor of {@link #exec()} followed by {@link CommandOutcome#exit()}.
     */
    @Deprecated
    public void run() {
        exec().exit();
    }

    /**
     * Executes this Bootique application, returning the object that denotes the outcome.
     *
     * @return an outcome of command execution.
     * @since 0.23
     */
    public CommandOutcome exec() {

        CommandOutcome o;

        try {
            // In case the app gets killed when command is running, let's use an explicit shutdown hook for cleanup.
            Thread shutdownThread = createJVMShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownThread);
            try {
                o = createRuntime().run();
            } finally {
                // run shutdown explicitly...
                shutdown(shutdownManager, bootLogger);
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
            }
        }
        // unwrap standard Guice exceptions...
        catch (CreationException ce) {
            o = processExceptions(ce.getCause(), ce);
        }
        catch (ProvisionException pe) {
            o = processExceptions(pe.getCause(), pe);
        }
        catch (Throwable th) {
            o = processExceptions(th, th);
        }

        // report error
        if (!o.isSuccess()) {
            if (o.getMessage() != null) {
                bootLogger.stderr(String.format("Error running command '%s': %s", getArgsAsString(), o.getMessage()));
            } else {
                bootLogger.stderr(String.format("Error running command '%s'", getArgsAsString()));
            }
            if (o.getException() != null) {
                // exception unrecognized, dump the details for users to analyze..
                bootLogger.stderr("Command exception: ", o.getException());
            }
        }

        return o;
    }

    protected Thread createJVMShutdownHook() {

        // resolve all services needed for shutdown eagerly and outside shutdown thread to ensure that shutdown hook
        // will not fail due to misconfiguration, etc.

        ShutdownManager shutdownManager = this.shutdownManager;
        BootLogger logger = this.bootLogger;

        return new Thread("bootique-shutdown") {

            @Override
            public void run() {
                shutdown(shutdownManager, logger);
            }
        };
    }

    protected void shutdown(ShutdownManager shutdownManager, BootLogger logger) {
        shutdownManager.shutdown().forEach((s, th) -> {
            logger.stderr(String.format("Error performing shutdown of '%s': %s", s.getClass().getSimpleName(),
                    th.getMessage()));
        });
    }

    protected CommandOutcome processExceptions(Throwable th, Throwable parentTh) {

        if (th instanceof BootiqueException) {
            // TODO: should we still print the stack trace via logger.trace?
            CommandOutcome o = ((BootiqueException) th).getOutcome();
            return th == parentTh ? o : CommandOutcome.failed(o.getExitCode(), o.getMessage(), parentTh);
        }

        String thMessage = th != null ? th.getMessage() : null;
        String message = thMessage != null ? "Command exception: '" + thMessage + "'." : "Command exception.";
        return CommandOutcome.failed(1, message, parentTh);
    }

    protected String getArgsAsString() {
        return Arrays.asList(args).stream().collect(joining(" "));
    }

    /**
     * @param runtime runtime started by Bootique.
     * @return the outcome of the command execution.
     * @deprecated since 0.23. Previously this method existed to catch and process run exceptions, but it doesn't
     * have wide enough scope for this, so exception processing was moved to {@link #exec()}.
     */
    @Deprecated
    protected CommandOutcome run(BQRuntime runtime) {
        try {
            return runtime.getRunner().run();
        }
        // handle startup Guice exceptions
        catch (ProvisionException e) {

            // TODO: a dependency on JOPT OptionException shouldn't be here
            return (e.getCause() instanceof OptionException) ? CommandOutcome.failed(1, e.getCause().getMessage())
                    : CommandOutcome.failed(1, e);
        }
    }

    protected BQRuntime createRuntime(Injector injector) {
        return new BQRuntime(injector);
    }

    protected BootLogger createBootLogger() {
        return new DefaultBootLogger(System.getProperty(DefaultEnvironment.TRACE_PROPERTY) != null);
    }

    protected ShutdownManager createShutdownManager() {
        return new DefaultShutdownManager(Duration.ofMillis(10000L));
    }

    protected Injector createInjector() {

        DeferredModulesSource modulesSource = new DeferredModulesSource();

        Collection<BQModule> bqModules = new ArrayList<>();

        // note that 'moduleMetadata' is invalid at this point; it will be initialized later in this method, which
        // is safe to do, as it won't be used until the Injector is created by the method caller.
        bqModules.add(coreModuleProvider(modulesSource).moduleBuilder().build());

        builderProviders().forEach(p -> bqModules.add(p.moduleBuilder().build()));

        if (autoLoadModules) {
            autoLoadedProviders().forEach(p -> bqModules.add(p.moduleBuilder().build()));
        }

        // now that all modules are collected, finish 'moduleMetadata' initialization
        modulesSource.init(bqModules);

        // convert to Guice modules respecting overrides, etc.
        Collection<Module> modules = new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
        return Guice.createInjector(modules);
    }

    protected Collection<BQModuleProvider> builderProviders() {
        return providers;
    }

    protected BQModuleProvider coreModuleProvider(Supplier<Collection<BQModule>> moduleSource) {
        return new BQModuleProvider() {

            @Override
            public Module module() {
                return BQCoreModule.builder().args(args)
                        .bootLogger(bootLogger)
                        .shutdownManager(shutdownManager)
                        .moduleSource(moduleSource).build();
            }

            @Override
            public String name() {
                return "Bootique";
            }

            @Override
            public BQModule.Builder moduleBuilder() {
                return BQModuleProvider.super
                        .moduleBuilder()
                        .description("The core of Bootique runtime.");
            }
        };
    }

    protected Collection<BQModuleProvider> autoLoadedProviders() {
        Collection<BQModuleProvider> modules = new ArrayList<>();
        ServiceLoader.load(BQModuleProvider.class).forEach(p -> modules.add(p));
        return modules;
    }
}
