package io.bootique;

import com.google.inject.Binder;
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
import io.bootique.annotation.LogLevels;
import io.bootique.cli.Cli;
import io.bootique.cli.meta.CliApplication;
import io.bootique.cli.meta.CliCommand;
import io.bootique.cli.meta.CliOption;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandMetadata;
import io.bootique.command.DefaultCommandManager;
import io.bootique.command.HelpCommand;
import io.bootique.config.CliConfigurationSource;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.ConfigurationSource;
import io.bootique.env.DefaultEnvironment;
import io.bootique.env.Environment;
import io.bootique.help.DefaultHelpGenerator;
import io.bootique.help.HelpGenerator;
import io.bootique.jackson.DefaultJacksonService;
import io.bootique.jackson.JacksonService;
import io.bootique.jopt.JoptCliProvider;
import io.bootique.log.BootLogger;
import io.bootique.run.DefaultRunner;
import io.bootique.run.Runner;
import io.bootique.shutdown.DefaultShutdownManager;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.shutdown.ShutdownTimeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

/**
 * The main {@link Module} of Bootique DI runtime. Declares a minimal set of
 * services needed for a Bootique app to start: services for parsing command
 * line, reading configuration, selectign and running a Command.
 */
public class BQCoreModule implements Module {

    private String[] args;
    private BootLogger bootLogger;
    private Duration shutdownTimeout;

    private BQCoreModule() {
        this.shutdownTimeout = Duration.ofMillis(10000l);
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
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link Multibinder} for Bootique commands.
     * @since 0.12
     */
    public static Multibinder<Command> contributeCommands(Binder binder) {
        return Multibinder.newSetBinder(binder, Command.class);
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link Multibinder} for Bootique options.
     * @since 0.12
     */
    public static Multibinder<CliOption> contributeOptions(Binder binder) {
        return Multibinder.newSetBinder(binder, CliOption.class);
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link MapBinder} for Bootique properties.
     * @see EnvironmentProperties
     * @since 0.12
     */
    public static MapBinder<String, String> contributeProperties(Binder binder) {
        return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
    }

    /**
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link MapBinder} for values emulating environment variables.
     * @see EnvironmentVariables
     * @since 0.17
     */
    public static MapBinder<String, String> contributeVariables(Binder binder) {
        return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentVariables.class);
    }

    /**
     * Provides a way to set default log levels for specific loggers. These settings can be overridden via Bootique
     * configuration of whatever logging module you might use, like bootique-logback. This feature may be handy to
     * suppress chatty third-party loggers, but still allow users to turn them on via configuration.
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return {@link MapBinder} for Bootique properties.
     * @since 0.19
     */
    public static MapBinder<String, Level> contributeLogLevels(Binder binder) {
        return MapBinder.newMapBinder(binder, String.class, Level.class, LogLevels.class);
    }

    @Override
    public void configure(Binder binder) {

        // bind instances
        binder.bind(BootLogger.class).toInstance(Objects.requireNonNull(bootLogger));
        binder.bind(String[].class).annotatedWith(Args.class).toInstance(Objects.requireNonNull(args));
        binder.bind(Duration.class).annotatedWith(ShutdownTimeout.class)
                .toInstance(Objects.requireNonNull(shutdownTimeout));

        // too much code to create config factory.. extracting it in a provider
        // class...
        binder.bind(ConfigurationFactory.class).toProvider(JsonNodeConfigurationFactoryProvider.class).in(Singleton.class);

        // we can't bind Provider with @Provides, so declaring it here...
        binder.bind(Cli.class).toProvider(JoptCliProvider.class).in(Singleton.class);

        // trigger extension points creation and provide default contributions
        BQCoreModule.contributeProperties(binder);
        BQCoreModule.contributeVariables(binder);
        BQCoreModule.contributeCommands(binder).addBinding().to(HelpCommand.class).in(Singleton.class);
        BQCoreModule.contributeOptions(binder).addBinding().toInstance(createConfigOption());
        BQCoreModule.contributeLogLevels(binder);
    }

    CliOption createConfigOption() {
        return CliOption
                .builder(CliConfigurationSource.CONFIG_OPTION,
                        "Specifies YAML config location, which can be a file path or a URL.")
                .valueRequired("yaml_location").build();
    }

    @Provides
    @Singleton
    JacksonService provideJacksonService(BootLogger bootLogger) {
        return new DefaultJacksonService(bootLogger);
    }

    @Provides
    @Singleton
    Runner provideRunner(Cli cli, CommandManager commandManager) {
        return new DefaultRunner(cli, commandManager);
    }

    @Provides
    @Singleton
    ShutdownManager provideShutdownManager(@ShutdownTimeout Duration timeout) {
        return new DefaultShutdownManager(timeout);
    }

    @Provides
    @Singleton
    ConfigurationSource provideConfigurationSource(Cli cli, BootLogger bootLogger) {
        return new CliConfigurationSource(cli, bootLogger);
    }

    @Provides
    @Singleton
    @DefaultCommand
    Command provideDefaultCommand(HelpCommand helpCommand) {
        return helpCommand;
    }

    @Provides
    @Singleton
    HelpCommand provideHelpCommand(BootLogger bootLogger, Provider<HelpGenerator> helpGeneratorProvider) {
        return new HelpCommand(bootLogger, helpGeneratorProvider);
    }

    @Provides
    @Singleton
    CommandManager provideCommandManager(Set<Command> commands, @DefaultCommand Command defaultCommand) {
        return DefaultCommandManager.create(commands, defaultCommand);
    }

    @Provides
    @Singleton
    HelpGenerator provideHelpGenerator(CliApplication application) {
        return new DefaultHelpGenerator(application);
    }

    @Provides
    @Singleton
    CliApplication provideCliApplication(Set<Command> commands,
                                         Set<CliOption> options) {

        // TODO: deprecate CommandMetadata, replacing it with CliCommand
        Collection<CliCommand> cliCommands = new ArrayList<>();
        commands.forEach(c -> {
            CommandMetadata md = c.getMetadata();
            cliCommands.add(CliCommand
                    .builder(md.getName())
                    .description(md.getDescription())
                    .addOptions(md.getOptions())
                    .build());
        });

        return CliApplication.builder().addCommands(cliCommands).addOptions(options).build();
    }

    @Provides
    @Singleton
    Environment createEnvironment(@EnvironmentProperties Map<String, String> diProperties,
                                  @EnvironmentVariables Map<String, String> diVars) {
        return new DefaultEnvironment(diProperties, diVars);
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

        public Builder args(String[] args) {
            module.args = args;
            return this;
        }

        public Builder shutdownTimeout(Duration timeout) {
            module.shutdownTimeout = timeout;
            return this;
        }
    }

}
