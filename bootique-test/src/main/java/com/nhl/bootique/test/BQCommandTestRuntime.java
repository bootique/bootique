package com.nhl.bootique.test;

import java.util.function.Consumer;

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.command.CommandOutcome;

/**
 * @since 0.13
 */
public class BQCommandTestRuntime extends BQTestRuntime {

	public BQCommandTestRuntime(Consumer<Bootique> configurator) {
		super(configurator);
	}

	public CommandOutcome run(String... args) {

		BQRuntime runtime = createRuntime(args);
		try {
			return runtime.getRunner().run();
		} finally {
			runtime.shutdown();
		}
	}

}
