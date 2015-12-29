package com.nhl.bootique;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.jopt.Args;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.run.Runner;
import com.nhl.bootique.shutdown.ShutdownManager;

import joptsimple.OptionException;

/**
 * A wrapper around launcher DI container.
 */
public class BQRuntime {

	private Injector injector;

	public BQRuntime(Injector injector) {
		this.injector = injector;
	}

	public BootLogger getBootLogger() {
		return injector.getInstance(BootLogger.class);
	}

	public Runner getRunner() {
		return injector.getInstance(Runner.class);
	}

	public String[] getArgs() {
		return injector.getInstance(Key.get(String[].class, Args.class));
	}

	public String getArgsAsString() {
		return Arrays.asList(getArgs()).stream().collect(joining(" "));
	}

	/**
	 * Registers a JVM shutdown hook that is delegated to
	 * {@link ShutdownManager}.
	 * 
	 * @since 0.11
	 */
	public void addJVMShutdownHook() {

		// resolve all Injector services needed for shutdown eagerly and outside
		// shutdown thread to ensure that shutdown hook will not fail due to
		// misconfiguration, etc.

		ShutdownManager shutdownManager = injector.getInstance(ShutdownManager.class);
		BootLogger logger = getBootLogger();

		Runtime.getRuntime().addShutdownHook(new Thread("bootique-shutdown") {

			@Override
			public void run() {
				shutdownManager.shutdown().forEach((s, th) -> {
					logger.stderr(String.format("Error performing shutdown of '%s': %s", s.getClass().getSimpleName(),
							th.getMessage()));
				});
			}
		});
	}

	public CommandOutcome run() {
		try {
			return getRunner().run();
		}
		// handle startup Guice exceptions
		catch (ProvisionException e) {
			return (e.getCause() instanceof OptionException) ? CommandOutcome.failed(1, e.getCause().getMessage())
					: CommandOutcome.failed(1, e);
		}
	}

}
