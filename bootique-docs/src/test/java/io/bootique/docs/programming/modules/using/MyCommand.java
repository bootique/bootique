package io.bootique.docs.programming.modules.using;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;

public class MyCommand implements Command {
    @Override
    public CommandOutcome run(Cli cli) {
        return null;
    }
}
