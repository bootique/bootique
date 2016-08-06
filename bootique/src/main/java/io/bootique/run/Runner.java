package io.bootique.run;

import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;

/**
 * A service that finds and executes {@link Command} based on command line
 * options.
 */
public interface Runner {

	CommandOutcome run();
}
