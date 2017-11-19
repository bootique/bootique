package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.annotation.Args;
import io.bootique.annotation.DefaultCommand;
import io.bootique.annotation.EnvironmentProperties;
import io.bootique.annotation.EnvironmentVariables;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;
import io.bootique.command.Command;
import io.bootique.command.CommandDecorator;
import io.bootique.command.CommandDispatchThreadFactory;
import io.bootique.command.CommandManager;
import io.bootique.command.DefaultCommandManager;
import io.bootique.command.ExecutionPlanBuilder;
import io.bootique.config.CliConfigurationSource;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.ConfigurationSource;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.env.DeclaredVariable;
import io.bootique.env.DefaultEnvironment;
import io.bootique.env.Environment;
import io.bootique.help.DefaultHelpGenerator;
import io.bootique.help.HelpCommand;
import io.bootique.help.HelpGenerator;
import io.bootique.help.config.ConfigHelpGenerator;
import io.bootique.help.config.DefaultConfigHelpGenerator;
import io.bootique.help.config.HelpConfigCommand;
import io.bootique.jackson.DefaultJacksonService;
import io.bootique.jackson.JacksonService;
import io.bootique.jopt.JoptCliFactory;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.meta.config.ConfigHierarchyResolver;
import io.bootique.meta.config.ConfigMetadataCompiler;
import io.bootique.meta.module.ModulesMetadata;
import io.bootique.meta.module.ModulesMetadataCompiler;
import io.bootique.run.DefaultRunner;
import io.bootique.run.Runner;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.shutdown.ShutdownTimeout;
import io.bootique.terminal.FixedWidthTerminal;
import io.bootique.terminal.SttyTerminal;
import io.bootique.terminal.Terminal;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The main {@link Module} of Bootique DI runtime. Declares a minimal set of
 * services needed for a Bootique app to start: services for parsing command
 * line, reading configuration, selectign and running a Command.
 */
public class BQCoreModule implements Module {

    // TODO: duplicate of FormattedAppender.MIN_LINE_WIDTH
    private static final int TTY_MIN_COLUMNS = 40;
    private static final int TTY_DEFAULT_COLUMNS = 80;
    /**
     * Properties are used to exclude system env vars and properties.
     * It's a duplicate of constants in io.bootique.test.junit.BQTestRuntimeBuilder
     */
    private static final String EXCLUDE_SYSTEM_VARIABLES = "bq.core.excludeSystemVariables";
    private static final String EXCLUDE_SYSTEM_PROPERTIES = "bq.core.excludeSystemProperties";
    private String[] args;
    private ShutdownManager shutdownManager;
    private BootLogger bootLogger;
    private Supplier<Collection<BQModule>> modulesSource;

    private BQCoreModule() {
    }

    /**
     * @return a Builder instance to configure the module before using it to
     * initialize DI container.
     * @since 0.12
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns an instance of {@link BQCoreModuleExtender} used by downstream modules to load custom extensions to the
     * Bootique core module. Should be invoked from a downstream Module's "configure" method.
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return an instance of {@link BQCoreModuleExtender} that can be used to load custom extensions to the Bootique
     * core.
     * @since 0.22
     */
    public static BQCoreModuleExtender extend(Binder binder) {
        return new BQCoreModuleExtender(binder);
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link Multibinder} for Bootique commands.
     * @since 0.12
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#addCommand(Class)}.
     */
    @Deprecated
    public static Multibinder<Command> contributeCommands(Binder binder) {
        return extend(binder).contributeCommands();
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link Multibinder} for Bootique options.
     * @since 0.12
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#addOption(OptionMetadata)}.
     */
    @Deprecated
    public static Multibinder<OptionMetadata> contributeOptions(Binder binder) {
        return extend(binder).contributeOptions();
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link MapBinder} for Bootique properties.
     * @see EnvironmentProperties
     * @since 0.12
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#setProperty(String, String)}.
     */
    @Deprecated
    public static MapBinder<String, String> contributeProperties(Binder binder) {
        return extend(binder).contributeProperties();
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link MapBinder} for values emulating environment variables.
     * @see EnvironmentVariables
     * @since 0.17
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#setVar(String, String)}.
     */
    @Deprecated
    public static MapBinder<String, String> contributeVariables(Binder binder) {
        return extend(binder).contributeVariables();
    }

    /**
     * Provides a way to set default log levels for specific loggers. These settings can be overridden via Bootique
     * configuration of whatever logging module you might use, like bootique-logback. This feature may be handy to
     * suppress chatty third-party loggers, but still allow users to turn them on via configuration.
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link MapBinder} for Bootique properties.
     * @since 0.19
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#setLogLevel(String, Level)}.
     */
    @Deprecated
    public static MapBinder<String, Level> contributeLogLevels(Binder binder) {
        return extend(binder).contributeLogLevels();
    }

    /**
     * Binds an optional application description used in help messages, etc.
     *
     * @param description optional application description used in help messages, etc.
     * @param binder      DI binder passed to the Module that invokes this method.
     * @since 0.20
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#setApplicationDescription(String)}.
     */
    @Deprecated
    public static void setApplicationDescription(Binder binder, String description) {
        extend(binder).setApplicationDescription(description);
    }

    /**
     * Initializes optional default command that will be executed if no explicit command is matched.
     *
     * @param binder      DI binder passed to the Module that invokes this method.
     * @param commandType a class of the default command.
     * @since 0.20
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#setDefaultCommand(Class)}.
     */
    @Deprecated
    public static void setDefaultCommand(Binder binder, Class<? extends Command> commandType) {
        extend(binder).setDefaultCommand(commandType);
    }

    /**
     * Initializes optional default command that will be executed if no explicit command is matched.
     *
     * @param binder  DI binder passed to the Module that invokes this method.
     * @param command an instance of the default command.
     * @since 0.20
     * @deprecated since 0.22 use {@link #extend(Binder)} to get an extender object, and
     * then call {@link BQCoreModuleExtender#setDefaultCommand(Command)}.
     */
    @Deprecated
    public static void setDefaultCommand(Binder binder, Command command) {
        extend(binder).setDefaultCommand(command);
    }

    private static Optional<Command> defaultCommand(Injector injector) {
        Binding<Command> binding = injector.getExistingBinding(Key.get(Command.class, DefaultCommand.class));
        return binding != null ? Optional.of(binding.getProvider().get()) : Optional.empty();
    }

    @Override
    public void configure(Binder binder) {

        // trigger extension points creation and add default contributions
        BQCoreModule.extend(binder)
                .initAllExtensions()
                .addOption(createConfigOption());

        // bind instances
        binder.bind(BootLogger.class).toInstance(Objects.requireNonNull(bootLogger));
        binder.bind(ShutdownManager.class).toInstance(Objects.requireNonNull(shutdownManager));
        binder.bind(String[].class).annotatedWith(Args.class).toInstance(Objects.requireNonNull(args));

        // deprecated, kept for those users who may have injected this in their own code
        binder.bind(Duration.class).annotatedWith(ShutdownTimeout.class)
                .toInstance(Objects.requireNonNull(Duration.ofMillis(10000L)));

        // too much code to create config factory.. extracting it in a provider
        // class...
        binder.bind(ConfigurationFactory.class).toProvider(JsonNodeConfigurationFactoryProvider.class).in(Singleton.class);

        // while "help" is a special command, we still store it in the common list of commands,
        // so that "--help" is exposed as an explicit option
        BQCoreModule.extend(binder).addCommand(HelpCommand.class);
        BQCoreModule.extend(binder).addCommand(HelpConfigCommand.class);
    }

    OptionMetadata createConfigOption() {
        return OptionMetadata
                .builder(CliConfigurationSource.CONFIG_OPTION,
                        "Specifies YAML config location, which can be a file path or a URL.")
                .valueRequired("yaml_location").build();
    }

    @Provides
    @Singleton
    JacksonService provideJacksonService(TypesFactory<PolymorphicConfiguration> typesFactory) {
        return new DefaultJacksonService(typesFactory.getTypes());
    }

    @Provides
    @Singleton
    TypesFactory<PolymorphicConfiguration> provideConfigTypesFactory(BootLogger logger) {
        return new TypesFactory<>(getClass().getClassLoader(), PolymorphicConfiguration.class, logger);
    }

    @Provides
    @Singleton
    Runner provideRunner(Cli cli, CommandManager commandManager, ExecutionPlanBuilder execPlanBuilder) {
        return new DefaultRunner(cli, commandManager, execPlanBuilder);
    }

    @Provides
    @Singleton
    ConfigurationSource provideConfigurationSource(Cli cli, BootLogger bootLogger) {
        return new CliConfigurationSource(cli, bootLogger);
    }

    @Provides
    @Singleton
    HelpCommand provideHelpCommand(BootLogger bootLogger, Provider<HelpGenerator> helpGeneratorProvider) {
        return new HelpCommand(bootLogger, helpGeneratorProvider);
    }

    @Provides
    @Singleton
    HelpConfigCommand provideHelpConfigCommand(BootLogger bootLogger, Provider<ConfigHelpGenerator> helpGeneratorProvider) {
        return new HelpConfigCommand(bootLogger, helpGeneratorProvider);
    }

    @Provides
    @Singleton
    CliFactory provideCliFactory(
            Provider<CommandManager> commandManagerProvider,
            ApplicationMetadata applicationMetadata) {
        return new JoptCliFactory(commandManagerProvider, applicationMetadata);
    }

    @Provides
    @Singleton
    Cli provideCli(CliFactory cliFactory, @Args String[] args) {
        return cliFactory.createCli(args);
    }

    @Provides
    @Singleton
    ExecutionPlanBuilder provideExecutionPlanBuilder(
            Provider<CliFactory> cliFactoryProvider,
            Provider<CommandManager> commandManagerProvider,
            Map<Class<? extends Command>, CommandDecorator> commandDecorators,
            BootLogger logger) {

        Provider<ExecutorService> executorProvider = () -> Executors.newCachedThreadPool(new CommandDispatchThreadFactory());
        return new ExecutionPlanBuilder(cliFactoryProvider, commandManagerProvider, executorProvider, commandDecorators, logger);
    }

    @Provides
    @Singleton
    CommandManager provideCommandManager(
            Set<Command> commands,
            HelpCommand helpCommand,
            Injector injector) {

        // help command is bound, but default is optional, so check via injector...
        Optional<Command> defaultCommand = defaultCommand(injector);

        Map<String, Command> commandMap = new HashMap<>();

        commands.forEach(c -> {

            String name = c.getMetadata().getName();

            // if command's name matches default command, exclude it from command map (it is implicit)
            if (!defaultCommand.isPresent() || !defaultCommand.get().getMetadata().getName().equals(name)) {

                Command existing = commandMap.put(name, c);

                // complain on dupes
                if (existing != null && existing != c) {
                    String c1 = existing.getClass().getName();
                    String c2 = c.getClass().getName();

                    String message = String.format("More than one DI command named '%s'. Conflicting types: %s, %s.",
                            name, c1, c2);
                    throw new BootiqueException(1, message);
                }
            }
        });

        return new DefaultCommandManager(commandMap, defaultCommand, Optional.of(helpCommand));
    }

    @Provides
    @Singleton
    HelpGenerator provideHelpGenerator(ApplicationMetadata application, Terminal terminal) {

        int maxColumns = terminal.getColumns();
        if (maxColumns < TTY_MIN_COLUMNS) {
            maxColumns = TTY_DEFAULT_COLUMNS;
        }

        return new DefaultHelpGenerator(application, maxColumns);
    }

    @Provides
    @Singleton
    ConfigHelpGenerator provideConfigHelpGenerator(ModulesMetadata modulesMetadata, Terminal terminal) {

        int maxColumns = terminal.getColumns();
        if (maxColumns < TTY_MIN_COLUMNS) {
            maxColumns = TTY_DEFAULT_COLUMNS;
        }

        return new DefaultConfigHelpGenerator(modulesMetadata, maxColumns);
    }

    @Provides
    @Singleton
    ConfigHierarchyResolver provideConfigHierarchyResolver(TypesFactory<PolymorphicConfiguration> typesFactory) {
        return ConfigHierarchyResolver.create(typesFactory.getTypes());
    }

    @Provides
    @Singleton
    ModulesMetadata provideModulesMetadata(ConfigHierarchyResolver hierarchyResolver) {
        return new ModulesMetadataCompiler(new ConfigMetadataCompiler(hierarchyResolver::directSubclasses))
                .compile(this.modulesSource != null ? modulesSource.get() : Collections.emptyList());
    }

    @Provides
    @Singleton
    ApplicationMetadata provideApplicationMetadata(
            ApplicationDescription descriptionHolder,
            CommandManager commandManager,
            Set<OptionMetadata> options,
            Set<DeclaredVariable> declaredVars,
            ModulesMetadata modulesMetadata) {

        ApplicationMetadata.Builder builder = ApplicationMetadata
                .builder()
                .description(descriptionHolder.getDescription())
                .addOptions(options);

        commandManager.getCommands().values().forEach(c -> builder.addCommand(c.getMetadata()));

        // merge default command options with top-level app options
        commandManager.getDefaultCommand().ifPresent(c -> builder.addOptions(c.getMetadata().getOptions()));

        declaredVars.forEach(dv -> DeclaredVariableMetaCompiler
                .compileIfValid(dv, modulesMetadata)
                .ifPresent(builder::addVariable));

        return builder.build();
    }

    @Provides
    @Singleton
    Environment provideEnvironment(
            @EnvironmentProperties Map<String, String> diProperties,
            @EnvironmentVariables Map<String, String> diVars,
            Set<DeclaredVariable> declaredVariables,
            BootLogger logger) {

        DefaultEnvironment.Builder environment = DefaultEnvironment.builder(logger);

        if (Boolean.valueOf(diProperties.get(EXCLUDE_SYSTEM_PROPERTIES))) {
            environment.excludeSystemProperties();
        }
        if (Boolean.valueOf(diProperties.containsKey(EXCLUDE_SYSTEM_VARIABLES))) {
            environment.excludeSystemVariables();
        }

        return environment.diProperties(diProperties)
                .diVariables(diVars)
                .declaredVariables(declaredVariables)
                .build();
    }

    @Provides
    @Singleton
    Terminal provideTerminal(BootLogger bootLogger) {

        // very simple OS test...
        boolean isUnix = "/".equals(System.getProperty("file.separator"));
        return isUnix ? new SttyTerminal(bootLogger) : new FixedWidthTerminal(TTY_DEFAULT_COLUMNS);
    }

    public static class Builder {
        private BQCoreModule module;

        private Builder() {
            this.module = new BQCoreModule();
        }

        public BQCoreModule build() {
            return module;
        }

        public Builder bootLogger(BootLogger bootLogger) {
            module.bootLogger = bootLogger;
            return this;
        }

        public Builder shutdownManager(ShutdownManager shutdownManager) {
            module.shutdownManager = shutdownManager;
            return this;
        }

        /**
         * Sets a supplier of the app modules collection. It has to be provided externally by Bootique code that
         * assembles the stack. We have no way of discovering this information when inside the DI container.
         *
         * @param modulesSource a supplier of module collection.
         * @return this builder instance.
         * @since 0.21
         */
        public Builder moduleSource(Supplier<Collection<BQModule>> modulesSource) {
            module.modulesSource = modulesSource;
            return this;
        }

        public Builder args(String[] args) {
            module.args = args;
            return this;
        }
    }

}
