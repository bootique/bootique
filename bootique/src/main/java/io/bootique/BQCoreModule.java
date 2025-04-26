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

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.annotation.*;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;
import io.bootique.command.*;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.config.jackson.*;
import io.bootique.config.jackson.merger.InPlaceLeftHandMerger;
import io.bootique.config.jackson.merger.JsonConfigurationMerger;
import io.bootique.config.jackson.parser.*;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.Provides;
import io.bootique.di.spi.DIJsonConfigurationFactory;
import io.bootique.di.spi.DefaultInjector;
import io.bootique.env.DeclaredVariable;
import io.bootique.env.DefaultEnvironment;
import io.bootique.env.Environment;
import io.bootique.help.DefaultHelpGenerator;
import io.bootique.help.HelpCommand;
import io.bootique.help.HelpGenerator;
import io.bootique.help.ValueObjectDescriptor;
import io.bootique.help.config.ConfigHelpGenerator;
import io.bootique.help.config.DefaultConfigHelpGenerator;
import io.bootique.help.config.HelpConfigCommand;
import io.bootique.jackson.DefaultJacksonService;
import io.bootique.jackson.JacksonService;
import io.bootique.jopt.JoptCliFactory;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.ApplicationMetadataFactory;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.meta.config.ConfigHierarchyResolver;
import io.bootique.meta.config.ConfigMetadataCompiler;
import io.bootique.meta.module.ModulesMetadata;
import io.bootique.meta.module.ModulesMetadataCompiler;
import io.bootique.run.DefaultRunner;
import io.bootique.run.Runner;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.terminal.FixedWidthTerminal;
import io.bootique.terminal.SttyTerminal;
import io.bootique.terminal.Terminal;
import io.bootique.value.Bytes;
import io.bootique.value.Duration;
import io.bootique.value.Percent;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * The main {@link BQModule} of Bootique DI runtime. Declares a minimal set of services needed for a Bootique app to
 * start: parsing command line, reading configuration, findings and running a Command.
 */
public class BQCoreModule implements BQModule {

    private static final int TTY_MIN_COLUMNS = 40;
    private static final int TTY_DEFAULT_COLUMNS = 80;

    // Internal properties used to exclude system env vars and properties by BQTestRuntimeBuilder (JUnit 4) and
    //  TestRuntumeBuilder (JUnit 5)
    private static final String EXCLUDE_SYSTEM_VARIABLES = "bq.core.excludeSystemVariables";
    private static final String EXCLUDE_SYSTEM_PROPERTIES = "bq.core.excludeSystemProperties";

    private final String[] args;
    private final BootLogger bootLogger;
    private final ShutdownManager shutdownManager;
    private final Supplier<Collection<ModuleCrate>> modulesSource;

    protected BQCoreModule(
            String[] args,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Supplier<Collection<ModuleCrate>> modulesSource) {

        this.args = Objects.requireNonNull(args);
        this.bootLogger = Objects.requireNonNull(bootLogger);
        this.shutdownManager = Objects.requireNonNull(shutdownManager);
        this.modulesSource = Objects.requireNonNull(modulesSource);
    }

    /**
     * Returns an instance of {@link BQCoreModuleExtender} used by downstream modules to load custom extensions to the
     * Bootique core module. Should be invoked from a downstream Module's "configure" method.
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return an instance of {@link BQCoreModuleExtender} that can be used to load custom extensions to the Bootique
     * core.
     */
    public static BQCoreModuleExtender extend(Binder binder) {
        return new BQCoreModuleExtender(binder);
    }

    private static Optional<Command> defaultCommand(Injector injector) {
        // default is optional, so check via injector whether it is bound...
        Key<Command> key = Key.get(Command.class, DefaultCommand.class);
        if (injector.hasProvider(key)) {
            Provider<Command> commandProvider = injector.getProvider(key);
            return Optional.of(commandProvider.get());
        }
        return Optional.empty();
    }

    /**
     * @since 3.0
     */
    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("The core of Bootique runtime.")
                .build();
    }

    @Override
    public void configure(Binder binder) {

        BQCoreModule.extend(binder)
                .initAllExtensions()
                .addValueObjectsDescriptors(createValueObjectsDescriptorsMap())
                .addOption(createConfigOption())
                .addCommand(HelpConfigCommand.class)

                // standard config formats
                .addConfigFormatParser(JsonConfigurationFormatParser.class)
                .addConfigFormatParser(YamlConfigurationFormatParser.class)

                // standard config loaders
                .addConfigLoader(DIConfigurationLoader.class)
                .addConfigLoader(CliConfigurationLoader.class)
                .addConfigLoader(CliCustomOptionsConfigurationLoader.class)
                .addConfigLoader(PropertiesConfigurationLoader.class);

        // bind instances
        binder.bind(BootLogger.class).toInstance(bootLogger);
        binder.bind(ShutdownManager.class).toInstance(shutdownManager);
        binder.bind(String[].class, Args.class).toInstance(args);
    }

    OptionMetadata createConfigOption() {
        return OptionMetadata
                .builder(CliConfigurationLoader.CONFIG_OPTION,
                        "Specifies YAML config location, which can be a file path or a URL.")
                .valueRequired("yaml_location").build();
    }

    @Provides
    @Singleton
    ConfigurationFactory provideConfigurationFactory(
            Set<JsonConfigurationLoader> loaders,
            TypesFactory<PolymorphicConfiguration> typesFactory,
            Injector injector) {

        JsonNode root = JsonConfigurationLoader.load(loaders);
        bootLogger.trace(() -> "Merged configuration: " + root.toString());

        // preregister all explicitly declared polymorphic configurations for injection, as we won't be
        // able to identify them on the fly
        Collection injectionEnabledTypes = typesFactory.getTypes();
        return DIJsonConfigurationFactory.of(root, (DefaultInjector) injector, injectionEnabledTypes);
    }

    @Provides
    @Singleton
    JsonConfigurationParser provideJsonConfigurationParser(Set<ConfigurationFormatParser> parsers) {
        return new MultiFormatJsonNodeParser(parsers);
    }

    @Provides
    @Singleton
    JsonConfigurationMerger provideJsonConfigurationMerger() {
        return new InPlaceLeftHandMerger(bootLogger);
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
            Set<CommandRefDecorated> commandDecorators,
            BootLogger logger,
            ShutdownManager shutdownManager) {

        Provider<ExecutorService> executorProvider = () -> {
            ExecutorService service = Executors.newCachedThreadPool(new CommandDispatchThreadFactory());
            return shutdownManager.onShutdown(service, ExecutorService::shutdownNow);
        };

        Map<Class<? extends Command>, CommandDecorator> merged = ExecutionPlanBuilder.mergeDecorators(commandDecorators);
        return new ExecutionPlanBuilder(cliFactoryProvider, commandManagerProvider, executorProvider, merged, logger);
    }

    @Provides
    @Singleton
    CommandManager provideCommandManager(
            Set<Command> commands,
            HelpCommand helpCommand,
            Injector injector) {

        return new CommandManagerBuilder<>(commands)
                .defaultCommand(defaultCommand(injector))
                .helpCommand(helpCommand)
                .build();
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
    ModulesMetadata provideModulesMetadata(
            BootLogger logger,
            ConfigHierarchyResolver hierarchyResolver,
            Map<Class<?>, ValueObjectDescriptor> valueObjectDescriptors) {

        ConfigMetadataCompiler configCompiler =
                new ConfigMetadataCompiler(logger, hierarchyResolver::directSubclasses, valueObjectDescriptors);
        Collection<ModuleCrate> modules = modulesSource.get();
        return new ModulesMetadataCompiler(configCompiler).compile(modules);
    }

    @Provides
    @Singleton
    ApplicationMetadata provideApplicationMetadata(@BQInternal ApplicationMetadata internalMetadata) {
        // separation of publicly-exposed and internal metadata allows to override it in submodules, and yet have access
        // to the core metadata when building a custom one
        return internalMetadata;
    }

    @BQInternal
    @Provides
    @Singleton
    ApplicationMetadata provideInternalApplicationMetadata(
            BootLogger logger,
            ApplicationDescription descriptionHolder,
            CommandManager commandManager,
            Set<OptionMetadata> options,
            Set<DeclaredVariable> declaredVars,
            ModulesMetadata modulesMetadata) {

        return ApplicationMetadataFactory.of(
                logger,
                descriptionHolder.getDescription(),
                commandManager,
                options,
                declaredVars,
                modulesMetadata);
    }

    @Provides
    @Singleton
    Environment provideEnvironment(
            @EnvironmentProperties Map<String, String> diProperties,
            @EnvironmentVariables Map<String, String> diVars,
            Set<DeclaredVariable> declaredVariables) {

        DefaultEnvironment.Builder environment = DefaultEnvironment.builder();

        if (Boolean.parseBoolean(diProperties.get(EXCLUDE_SYSTEM_PROPERTIES))) {
            environment.excludeSystemProperties();
        }
        if (diProperties.containsKey(EXCLUDE_SYSTEM_VARIABLES)) {
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

    private Map<Class<?>, ValueObjectDescriptor> createValueObjectsDescriptorsMap() {
        Map<Class<?>, ValueObjectDescriptor> descriptors = new HashMap<>();
        descriptors.put(Bytes.class, new ValueObjectDescriptor("bytes expression, e.g. 5b, 23MB, 12gigabytes"));
        descriptors.put(Duration.class, new ValueObjectDescriptor("duration expression, e.g. 5ms, 2s, 1hr"));
        descriptors.put(Percent.class, new ValueObjectDescriptor("percent expression, e.g. 15%, 75%"));

        return descriptors;
    }
}
