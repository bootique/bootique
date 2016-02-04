package com.nhl.bootique;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.annotation.Args;
import com.nhl.bootique.annotation.DefaultCommand;
import com.nhl.bootique.annotation.EnvironmentProperties;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.cli.CliOption;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandManager;
import com.nhl.bootique.command.DefaultCommandManager;
import com.nhl.bootique.command.HelpCommand;
import com.nhl.bootique.config.CliConfigurationSource;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.config.YamlConfigurationFactory;
import com.nhl.bootique.env.DefaultEnvironment;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.DefaultJacksonService;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.jopt.JoptCliProvider;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.run.DefaultRunner;
import com.nhl.bootique.run.Runner;
import com.nhl.bootique.shutdown.DefaultShutdownManager;
import com.nhl.bootique.shutdown.ShutdownManager;
import com.nhl.bootique.shutdown.ShutdownTimeout;

/**
 * The main {@link Module} of Bootique DI runtime. Declares a minimal set of
 * services needed for a Bootique app to start: services for parsing command
 * line, reading configuration, selectign and running a Command.
 */
public class BQCoreModule implements Module {

	private String[] args;
	private BootLogger bootLogger;
	private Duration shutdownTimeout;

	/**
	 * @since 0.12
	 * @return a Builder instance to configure the module before using it to
	 *         initialize DI container.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * @since 0.12
	 * @param binder
	 *            DI binder passed to the Module that invokes this method.
	 * @return {@link Multibinder} for Bootique commands.
	 */
	public static Multibinder<Command> contributeCommands(Binder binder) {
		return Multibinder.newSetBinder(binder, Command.class);
	}

	/**
	 * @since 0.12
	 * @param binder
	 *            DI binder passed to the Module that invokes this method.
	 * @return {@link Multibinder} for Bootique options.
	 */
	public static Multibinder<CliOption> contributeOptions(Binder binder) {
		return Multibinder.newSetBinder(binder, CliOption.class);
	}

	/**
	 * @since 0.12
	 * @param binder
	 *            DI binder passed to the Module that invokes this method.
	 * @return {@link MapBinder} for Bootique properties.
	 * @see EnvironmentProperties
	 */
	public static MapBinder<String, String> contributeProperties(Binder binder) {
		return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
	}

	private BQCoreModule() {
		this.shutdownTimeout = Duration.ofMillis(10000l);
	}

	@Override
	public void configure(Binder binder) {

		// bind instances
		binder.bind(BootLogger.class).toInstance(Objects.requireNonNull(bootLogger));
		binder.bind(String[].class).annotatedWith(Args.class).toInstance(Objects.requireNonNull(args));
		binder.bind(Duration.class).annotatedWith(ShutdownTimeout.class)
				.toInstance(Objects.requireNonNull(shutdownTimeout));

		// bind singleton types
		binder.bind(JacksonService.class).to(DefaultJacksonService.class);
		binder.bind(Runner.class).to(DefaultRunner.class).in(Singleton.class);
		binder.bind(ShutdownManager.class).to(DefaultShutdownManager.class).in(Singleton.class);
		binder.bind(Cli.class).toProvider(JoptCliProvider.class).in(Singleton.class);
		binder.bind(ConfigurationSource.class).to(CliConfigurationSource.class).in(Singleton.class);
		binder.bind(ConfigurationFactory.class).to(YamlConfigurationFactory.class).in(Singleton.class);
		binder.bind(Command.class).annotatedWith(DefaultCommand.class).to(HelpCommand.class).in(Singleton.class);

		// trigger extension points creation and provide default contributions
		BQCoreModule.contributeProperties(binder);
		BQCoreModule.contributeCommands(binder).addBinding().to(HelpCommand.class).in(Singleton.class);
		BQCoreModule.contributeOptions(binder).addBinding().toInstance(createConfigOption());
	}

	protected CliOption createConfigOption() {
		return CliOption.builder(CliConfigurationSource.CONFIG_OPTION, "Specifies YAML config file path.")
				.valueRequired("yaml_file").build();
	}

	@Provides
	@Singleton
	public CommandManager createCommandManager(Set<Command> commands, @DefaultCommand Command defaultCommand) {
		return DefaultCommandManager.create(commands, defaultCommand);
	}

	@Provides
	@Singleton
	public Environment createEnvironment(@EnvironmentProperties Map<String, String> diProperties) {
		return new DefaultEnvironment(diProperties);
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
