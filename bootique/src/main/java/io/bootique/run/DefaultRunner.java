package io.bootique.run;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandOutcome;
import io.bootique.command.ExecutionPlanBuilder;
import io.bootique.command.ManagedCommand;

public class DefaultRunner implements Runner {

    private Cli cli;
    private CommandManager commandManager;
    private ExecutionPlanBuilder executionPlanBuilder;

    public DefaultRunner(Cli cli, CommandManager commandManager, ExecutionPlanBuilder executionPlanBuilder) {
        this.cli = cli;
        this.commandManager = commandManager;
        this.executionPlanBuilder = executionPlanBuilder;
    }

    @Override
    public CommandOutcome run() {
        return getCommand().run(cli);
    }

    private Command getCommand() {
        return prepareForExecution(bareCommand());
    }

    private Command prepareForExecution(Command bareCommand) {
        return executionPlanBuilder.prepareForExecution(bareCommand);
    }

    private Command bareCommand() {
        if (cli.commandName() != null) {
            ManagedCommand explicitCommand = commandManager.getAllCommands().get(cli.commandName());
            if (explicitCommand == null || !explicitCommand.isPublic() || explicitCommand.isDefault()) {
                throw new IllegalStateException("Not a valid command: " + cli.commandName());
            }

            return explicitCommand.getCommand();
        }

        // command not found in CLI .. go through defaults

        return commandManager.getDefaultCommand() // 1. runtime default command
                .orElse(commandManager.getHelpCommand() // 2. help command
                        .orElse(cli -> CommandOutcome.succeeded())); // 3. placeholder noop command
    }

}
