/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique;

import io.bootique.command.CommandOutcome;
import io.bootique.di.*;
import io.bootique.env.DefaultEnvironment;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.shutdown.DefaultShutdownManager;
import io.bootique.shutdown.ShutdownManager;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;

/**
 * A main launcher class of Bootique. You may use this class as the main class to start the app. Or you may write your
 * own main method as follows:
 * <pre>
 * public static void main(String[] args) {
 *     Bootique.app(args).modules(...).exec().exit();
 * }
 * </pre>
 * or
 * <pre>
 * public static void main(String[] args) {
 *     Bootique.app(args).autoLoadModules().exec().exit();
 * }
 * </pre>
 */
public class Bootique {

    static final String CORE_PROVIDER_NAME = "Bootique";

    private final Collection<BQModuleProvider> providers;
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

    /**
     * A generic main method that autoloads available modules and runs Bootique stack. Useful for apps that don't
     * care to customize their "main()".
     *
     * @param args app arguments passed by the shell.
     */
    public static void main(String[] args) {
        Bootique.app(args).autoLoadModules().exec().exit();
    }

    /**
     * Starts a builder of Bootique runtime.
     *
     * @param args command-line arguments.
     * @return Bootique object that can be customized and then executed as an app via {@link #exec()} method.
     */
    public static Bootique app(String... args) {
        if (args == null) {
            args = new String[0];
        }

        return new Bootique(args);
    }

    /**
     * Starts a builder of Bootique runtime, initializing it with provided command-line arguments.
     *
     * @param args command-line arguments.
     * @return Bootique object that can be customized and then executed as an app via {@link #exec()} method.
     */
    public static Bootique app(Collection<String> args) {
        if (args == null) {
            args = Collections.emptyList();
        }

        return app(Objects.requireNonNull(args).toArray(new String[0]));
    }

    /**
     * Optionally overrides Bootique's BootLogger.
     *
     * @param bootLogger a custom BootLogger. Has to be non-null.
     * @return this instance of Bootique.
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
     */
    public Bootique args(String... args) {
        if (args != null) {
            this.args = mergeArrays(this.args, args);
        }
        return this;
    }

    /**
     * Appends extra values to Bootique CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of Bootique
     */
    public Bootique args(Collection<String> args) {
        if (args != null) {
            this.args = mergeArrays(this.args, args.toArray(new String[0]));
        }
        return this;
    }

    /**
     * Instructs Bootique to load any modules available on classpath that expose {@link BQModuleProvider} provider.
     * Autoloaded modules will be used in default configuration. Factories within modules will of course be
     * configured dynamically from YAML.
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
     */
    public Bootique module(Class<? extends BQModule> moduleType) {
        Objects.requireNonNull(moduleType);
        providers.add(moduleProviderForType(moduleType));
        return this;
    }

    /**
     * Adds an array of Module types to the Bootique DI runtime. Each type will
     * be instantiated by Bootique and added to the DI container.
     *
     * @param moduleTypes custom Module classes to add to Bootique DI runtime.
     * @return this Bootique instance
     * @see #autoLoadModules()
     */
    @SafeVarargs
    public final Bootique modules(Class<? extends BQModule>... moduleTypes) {

        for (Class<? extends BQModule> c : moduleTypes) {
            module(c);
        }

        return this;
    }

    public Bootique module(BQModule m) {
        Objects.requireNonNull(m);
        providers.add(moduleProviderForModule(m));
        return this;
    }

    /**
     * Adds an array of Modules to the Bootique DI runtime.
     *
     * @param modules an array of modules to add to Bootiqie DI runtime.
     * @return this instance of {@link Bootique}.
     */
    public Bootique modules(BQModule... modules) {
        Arrays.asList(modules).forEach(this::module);
        return this;
    }

    /**
     * Adds a Module generated by the provider. Provider may optionally specify
     * that the Module overrides services in some other Module.
     *
     * @param moduleProvider a provider of Module and override spec.
     * @return this instance of {@link Bootique}.
     * @since 2.0
     */
    public Bootique moduleProvider(BQModuleProvider moduleProvider) {
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
    public final BQModuleOverrideBuilder<Bootique> override(Class<? extends BQModule>... overriddenTypes) {
        return new BQModuleOverrideBuilder<>() {

            @Override
            public Bootique with(Class<? extends BQModule> moduleType) {
                providers.add(moduleProviderForType(moduleType, overriddenTypes));
                return Bootique.this;
            }

            @Override
            public Bootique with(BQModule module) {
                providers.add(() -> ModuleCrate.of(module).overrides(overriddenTypes).build());
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
     */
    public BQRuntime createRuntime() {
        Injector injector = createInjector();
        BQRuntime runtime = createRuntime(injector);

        runtime.getInstance(Key.getSetOf(BQRuntimeListener.class)).forEach(sl -> sl.onRuntimeCreated(runtime));

        return runtime;
    }

    /**
     * Executes this Bootique application, returning the outcome object.
     *
     * @return an outcome of the app command execution.
     */
    public CommandOutcome exec() {

        CommandOutcome o;

        try {
            // In case the app gets killed when command is running, let's use an explicit shutdown hook for cleanup.
            Thread shutdownThread = createJVMShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownThread);
            try {
                o = createRuntime().run();

                // block exit if there are remaining tasks...
                if (o.forkedToBackground()) {
                    try {
                        Thread.currentThread().join();
                    } catch (InterruptedException e) {
                        // interruption of a running daemon is a normal event, so return success
                    }
                }

            } finally {
                // run shutdown explicitly...
                shutdown(shutdownManager, bootLogger);
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
            }
        } catch (DIRuntimeException ce) {
            // unwrap standard DI exceptions...
            o = processExceptions(ce.getCause(), ce);
        } catch (Throwable th) {
            o = processExceptions(th, th);
        }

        // report error
        if (!o.isSuccess()) {
            if (o.getMessage() != null) {
                bootLogger.stderr(String.format("Error running command '%s': %s", getArgsAsString(), o.getMessage()), o.getException());
            } else {
                bootLogger.stderr(String.format("Error running command '%s'", getArgsAsString()), o.getException());
            }
        }

        return o;
    }

    private Thread createJVMShutdownHook() {

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

    private void shutdown(ShutdownManager shutdownManager, BootLogger logger) {
        shutdownManager.shutdown().forEach((s, th) ->
                logger.stderr(String.format("Error performing shutdown of '%s': %s",
                        s.getClass().getSimpleName(),
                        th.getMessage() != null ? th.getMessage() : th.getClass().getName())));
    }

    private CommandOutcome processExceptions(Throwable th, Throwable parentTh) {


        if (th instanceof BootiqueException) {
            CommandOutcome originalOutcome = ((BootiqueException) th).getOutcome();

            // BootiqueException should be stripped of the exception cause and reported on a single line
            // TODO: should we still print the stack trace via logger.trace?
            return CommandOutcome.failed(originalOutcome.getExitCode(), originalOutcome.getMessage());
        }

        String thMessage = th != null ? th.getMessage() : null;
        String message = thMessage != null ? "Command exception: '" + thMessage + "'." : "Command exception.";
        return CommandOutcome.failed(1, message, parentTh);
    }

    private String getArgsAsString() {
        return String.join(" ", args);
    }

    private BQRuntime createRuntime(Injector injector) {
        return new BQRuntime(injector);
    }

    private BootLogger createBootLogger() {
        return new DefaultBootLogger(System.getProperty(DefaultEnvironment.TRACE_PROPERTY) != null);
    }

    private ShutdownManager createShutdownManager() {
        return new DefaultShutdownManager(Duration.ofMillis(10000L));
    }

    Injector createInjector() {

        Collection<ModuleCrate> crates = new HashSet<>();
        DeferredModulesSource modulesSource = new DeferredModulesSource();

        // BQCoreModule requires a couple of explicit services that can not be initialized within the module itself
        BQCoreModule coreModule = new BQCoreModule(args, bootLogger, shutdownManager, modulesSource);

        // Note that BQCoreModule is invalid at this point due to uninitialized "modulesSource". It will be
        // initialized below, which is safe to do, as it won't be used until the Injector is returned to the method caller.
        crates.add(coreModule.moduleCrate());

        crates.addAll(moduleProviderDependencies(providers));
        if (autoLoadModules) {
            autoLoadedProviders().forEach(p -> crates.add(p.moduleCrate()));
        }

        List<ModuleCrate> sortedCrates = new ModulesSorter(bootLogger).uniqueCratesInLoadOrder(crates);

        // before returning the Injector, finish 'moduleMetadata' initialization
        modulesSource.init(sortedCrates);

        BQModule[] modules = sortedCrates.stream().map(ModuleCrate::getModule).toArray(i -> new BQModule[i]);
        return DIBootstrap.injectorBuilder(modules).build();
    }

    Collection<BQModuleProvider> autoLoadedProviders() {
        Collection<BQModuleProvider> modules = new ArrayList<>();
        ServiceLoader.load(BQModuleProvider.class).forEach(modules::add);
        return modules;
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

    static Collection<ModuleCrate> moduleProviderDependencies(Collection<BQModuleProvider> providers) {
        return moduleProviderDependencies(providers, new HashSet<>());
    }

    private static Set<ModuleCrate> moduleProviderDependencies(
            Collection<BQModuleProvider> providers,
            Set<ModuleCrate> crates) {

        for (BQModuleProvider moduleProvider : providers) {
            ModuleCrate next = moduleProvider.moduleCrate();
            if (crates.add(next)) {
                Collection<BQModuleProvider> dependencies = moduleProvider.dependencies();
                if (!dependencies.isEmpty()) {
                    crates.addAll(moduleProviderDependencies(dependencies, crates));
                }
            }
        }

        return crates;
    }

    static BQModuleProvider moduleProviderForModule(BQModule module) {
        // very often modules are also self-providers and can give us meaningful metadata
        return module instanceof BQModuleProvider
                ? (BQModuleProvider) module
                : () -> ModuleCrate.of(module).providerName(CORE_PROVIDER_NAME).build();
    }

    static BQModuleProvider moduleProviderForType(Class<? extends BQModule> moduleType) {
        // very often modules are also self-providers and can give us meaningful metadata
        return BQModuleProvider.class.isAssignableFrom(moduleType)
                ? (BQModuleProvider) moduleForType(moduleType)
                : () -> ModuleCrate.of(moduleForType(moduleType)).providerName(CORE_PROVIDER_NAME).build();
    }

    @SafeVarargs
    static BQModuleProvider moduleProviderForType(
            Class<? extends BQModule> moduleType,
            Class<? extends BQModule>... overriddenTypes) {
        
        return () -> ModuleCrate
                .of(moduleProviderForType(moduleType).moduleCrate())
                .overrides(overriddenTypes).build();
    }

    static BQModule moduleForType(Class<? extends BQModule> moduleType) {
        try {
            return moduleType.getDeclaredConstructor().newInstance();
        } catch (
                InstantiationException |
                IllegalAccessException |
                NoSuchMethodException |
                InvocationTargetException e) {
            throw new RuntimeException("Error instantiating Module of type: " + moduleType.getName(), e);
        }
    }
}
