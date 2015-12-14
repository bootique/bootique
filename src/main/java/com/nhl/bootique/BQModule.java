package com.nhl.bootique;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.ConfigCommand;
import com.nhl.bootique.command.DefaultCommand;
import com.nhl.bootique.command.FailoverHelpCommand;
import com.nhl.bootique.command.HelpCommand;
import com.nhl.bootique.config.CliConfigurationSource;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.DefaultEnvironment;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.env.EnvironmentProperties;
import com.nhl.bootique.factory.FactoryConfigurationService;
import com.nhl.bootique.factory.YamlFactoryConfigurationService;
import com.nhl.bootique.jackson.DefaultJacksonService;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.jopt.Args;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.jopt.OptionsProvider;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;
import com.nhl.bootique.log.LogbackFactory;
import com.nhl.bootique.run.DefaultRunner;
import com.nhl.bootique.run.Runner;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class BQModule implements Module {

	private String[] args;
	private String logConfigPrefix;

	/**
	 * Utility method for the bundle modules to bind their own default
	 * properties.
	 */
	public static MapBinder<String, String> propertiesBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
	}

	public BQModule(String[] args, String logConfigPrefix) {
		this.args = args;
		this.logConfigPrefix = logConfigPrefix;
	}

	@Override
	public void configure(Binder binder) {

		// this logger is used during boot sequence
		binder.bind(BootLogger.class).to(DefaultBootLogger.class);

		// this logger is configured from YAML and is used by everybody...

		// (binding a dummy class to trigger eager init of Logback as @Provides
		// below can not be invoked eagerly)
		binder.bind(LogInitTrigger.class).asEagerSingleton();

		binder.bind(JacksonService.class).to(DefaultJacksonService.class);
		binder.bind(String[].class).annotatedWith(Args.class).toInstance(args);
		binder.bind(Runner.class).to(DefaultRunner.class).in(Singleton.class);
		binder.bind(Options.class).toProvider(OptionsProvider.class).in(Singleton.class);
		binder.bind(ConfigurationSource.class).to(CliConfigurationSource.class).in(Singleton.class);
		binder.bind(FactoryConfigurationService.class).to(YamlFactoryConfigurationService.class);
		binder.bind(Environment.class).to(DefaultEnvironment.class);

		binder.bind(Command.class).annotatedWith(DefaultCommand.class).to(FailoverHelpCommand.class)
				.in(Singleton.class);

		Multibinder<Command> commands = Multibinder.newSetBinder(binder, Command.class);

		commands.addBinding().to(HelpCommand.class);
		commands.addBinding().to(ConfigCommand.class);

		// don't bind anything to properties yet, but still declare the binding
		propertiesBinder(binder);
	}

	@Provides
	public Logger configLogbackRootLogger(FactoryConfigurationService factoryConfig) {
		LoggerContext context = createLogbackContext();
		return factoryConfig.factory(LogbackFactory.class, logConfigPrefix).createRootLogger(context);
	}

	// copied from Dropwizard. See DW DefaultLoggingFactory and
	// http://jira.qos.ch/browse/SLF4J-167. Though presumably Bootique calls
	// this from the main thread, so we should not be affected by the issue.
	protected LoggerContext createLogbackContext() {
		long startTime = System.nanoTime();
		while (true) {
			ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();

			if (iLoggerFactory instanceof LoggerContext) {
				return (LoggerContext) iLoggerFactory;
			}

			if ((System.nanoTime() - startTime) > 10_000_000) {
				throw new IllegalStateException("Unable to acquire the logger context");
			}

			try {
				Thread.sleep(100_000);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	static class LogInitTrigger {

		@Inject
		public LogInitTrigger(Logger rootLogger) {
			rootLogger.info("Logback started");
		}
	}
}
