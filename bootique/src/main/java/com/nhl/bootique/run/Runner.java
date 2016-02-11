package com.nhl.bootique.run;

import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;

/**
 * A service that finds and executes {@link Command} based on command line
 * options.
 */
public interface Runner {

	CommandOutcome run();
}
