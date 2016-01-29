package com.nhl.bootique;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.annotation.EnvironmentProperties;
import com.nhl.bootique.cli.CliOption;
import com.nhl.bootique.command.Command;

/**
 * A helper class used inside Modules that allows to contribute commands and
 * properties to Bootique runtime. Create and invoke this binder inside your
 * Module's "configure" method to provide your own properties and/or Commands.
 * 
 * @since 0.8
 */
public class BQBinder {

	public static BQBinder contributeTo(Binder binder) {
		return new BQBinder(binder);
	}

	private Binder binder;

	BQBinder(Binder binder) {
		this.binder = binder;
	}

	MapBinder<String, String> propsBinder() {
		return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
	}

	/**
	 * Utility method to contribute custom environment properties to DI.
	 * 
	 * @param key
	 *            environment parameter name.
	 * @param value
	 *            environment parameter value.
	 */
	public void property(String key, String value) {
		propsBinder().addBinding(key).toInstance(value);
	}

	/**
	 * Utility method to contribute custom commands to DI.
	 * 
	 * @param commands
	 *            {@link Command} types for singleton commands to add to
	 *            Bootique.
	 */
	@SafeVarargs
	public final void commandTypes(Class<? extends Command>... commands) {
		commandTypes(Arrays.asList(Preconditions.checkNotNull(commands)));
	}

	/**
	 * Utility method to contribute custom commands to DI.
	 * 
	 * @param commands
	 *            {@link Command} types for singleton commands to add to
	 *            Bootique.
	 */
	public void commandTypes(Collection<Class<? extends Command>> commands) {
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder, Command.class);
		Preconditions.checkNotNull(commands).forEach(ct -> commandBinder.addBinding().to(ct));
	}

	/**
	 * Utility method to contribute custom commands to DI.
	 * 
	 * @param commands
	 *            {@link Command} instances to add to Bootique.
	 */
	public void commands(Collection<? extends Command> commands) {
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder, Command.class);
		Preconditions.checkNotNull(commands).forEach(c -> commandBinder.addBinding().toInstance(c));
	}

	/**
	 * Binds global options not explicitly associated with Commands
	 * 
	 * @since 0.12
	 * @param options
	 *            an array of options to recognize when parsing command line,
	 *            which should be merged into existing set of options.
	 */
	public void options(CliOption... options) {
		options(Arrays.asList(Preconditions.checkNotNull(options)));
	}

	/**
	 * Binds global options not explicitly associated with Commands
	 * 
	 * @since 0.12
	 * @param options
	 *            a collection of options to recognize when parsing command
	 *            line, which should be merged into existing set of options.
	 */
	public void options(Collection<CliOption> options) {
		Multibinder<CliOption> optionsBinder = Multibinder.newSetBinder(binder, CliOption.class);
		Preconditions.checkNotNull(options).forEach(o -> optionsBinder.addBinding().toInstance(o));
	}
}
