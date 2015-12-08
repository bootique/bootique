package com.nhl.launcher;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.nhl.launcher.jopt.Args;

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

	public String[] getArgs() {
		return injector.getInstance(Key.get(String[].class, Args.class));
	}

	public String getArgsAsString() {
		return Arrays.asList(getArgs()).stream().collect(joining(" "));
	}

}
