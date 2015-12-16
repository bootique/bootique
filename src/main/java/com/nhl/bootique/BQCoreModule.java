package com.nhl.bootique;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.ConfigCommand;
import com.nhl.bootique.command.DefaultCommand;
import com.nhl.bootique.command.FailoverHelpCommand;
import com.nhl.bootique.command.HelpCommand;
import com.nhl.bootique.config.CliConfigurationSource;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.DefaultEnvironment;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.factory.FactoryConfigurationService;
import com.nhl.bootique.factory.YamlFactoryConfigurationService;
import com.nhl.bootique.jackson.DefaultJacksonService;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.jopt.Args;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.jopt.OptionsProvider;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.run.DefaultRunner;
import com.nhl.bootique.run.Runner;

public class BQCoreModule implements Module {

	private String[] args;
	private BootLogger bootLogger;

	public BQCoreModule(String[] args, BootLogger bootLogger) {
		this.args = args;
		this.bootLogger = bootLogger;
	}

	@Override
	public void configure(Binder binder) {

		binder.bind(BootLogger.class).toInstance(bootLogger);
		binder.bind(JacksonService.class).to(DefaultJacksonService.class);
		binder.bind(String[].class).annotatedWith(Args.class).toInstance(args);
		binder.bind(Runner.class).to(DefaultRunner.class).in(Singleton.class);
		binder.bind(Options.class).toProvider(OptionsProvider.class).in(Singleton.class);
		binder.bind(ConfigurationSource.class).to(CliConfigurationSource.class).in(Singleton.class);
		binder.bind(FactoryConfigurationService.class).to(YamlFactoryConfigurationService.class);
		binder.bind(Environment.class).to(DefaultEnvironment.class);

		binder.bind(Command.class).annotatedWith(DefaultCommand.class).to(FailoverHelpCommand.class)
				.in(Singleton.class);

		BQBinder contribBinder = BQBinder.contributeTo(binder);

		// don't bind anything to properties yet, but still declare the binding
		contribBinder.propsBinder();
		contribBinder.commandTypes(HelpCommand.class, ConfigCommand.class);
	}
}
