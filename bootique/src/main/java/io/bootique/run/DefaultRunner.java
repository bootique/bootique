package io.bootique.run;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandOutcome;

public class DefaultRunner implements Runner {

    private Cli cli;
    private CommandManager commandManager;

    public DefaultRunner(Cli cli, CommandManager commandManager) {
        this.cli = cli;
        this.commandManager = commandManager;
    }

    @Override
    public CommandOutcome run() {
        return getCommand().run(cli);
    }

    private Command getCommand() {

        if (cli.commandName() != null) {
            Command explicitCommand = commandManager.getCommands().get(cli.commandName());
            if (explicitCommand == null) {
                throw new IllegalStateException("Not a valid command: " + cli.commandName());
            }

            return explicitCommand;
        }

        // command not found in CLI .. go through defaults

        return commandManager.getDefaultCommand() // 1. runtime default command
                .orElse(commandManager.getHelpCommand() // 2. help command
                        .orElse(cli -> CommandOutcome.succeeded())); // 3. placeholder noop command
    }

}
