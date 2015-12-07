package com.nhl.launcher;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import org.apache.cayenne.di.Injector;

import com.nhl.launcher.jopt.DefaultOptionsLoader;

/**
 * A wrapper around launcher DI container.
 */
class LauncherRuntime {

	private Injector injector;

	public LauncherRuntime(Injector injector) {
		this.injector = injector;
	}

	public Runner getRunner() {
		return injector.getInstance(Runner.class);
	}

	public String getArgsAsString() {
		return Arrays.asList(injector.getInstance(DefaultOptionsLoader.ARGS_KEY)).stream().collect(joining(" "));
	}

}
