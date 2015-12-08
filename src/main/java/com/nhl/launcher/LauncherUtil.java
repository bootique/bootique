package com.nhl.launcher;

import org.apache.cayenne.di.Binder;

import com.nhl.launcher.command.Command;
import com.nhl.launcher.command.FailoverHelpCommand;

public class LauncherUtil {

	public static void bindCommand(Binder binder, Class<? extends Command> command) {
		binder.<Command> bindList(BootstrapModule.COMMANDS_KEY).add(command).before(FailoverHelpCommand.class);
	}
	
	public static void bindCommand(Binder binder, Command command) {
		binder.<Command> bindList(BootstrapModule.COMMANDS_KEY).add(command).before(FailoverHelpCommand.class);
	}
}
