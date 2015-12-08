package com.nhl.launcher;

import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

import com.nhl.launcher.command.ConfigCommand;
import com.nhl.launcher.command.FailoverHelpCommand;
import com.nhl.launcher.command.HelpCommand;
import com.nhl.launcher.jopt.DefaultOptionsLoader;
import com.nhl.launcher.jopt.OptionsLoader;

public class BootstrapModule implements Module {

	public static final String COMMANDS_KEY = "com.nhl.launcher.commands";
	private String[] args;

	public BootstrapModule(String[] args) {
		this.args = args;
	}

	@Override
	public void configure(Binder binder) {

		binder.bind(DefaultOptionsLoader.ARGS_KEY).toInstance(args);
		binder.bind(Runner.class).to(DefaultRunner.class);
		binder.bind(OptionsLoader.class).to(DefaultOptionsLoader.class);

		binder.bindList(COMMANDS_KEY).add(FailoverHelpCommand.class).add(HelpCommand.class)
				.before(FailoverHelpCommand.class).add(ConfigCommand.class).before(FailoverHelpCommand.class);
	}
}
