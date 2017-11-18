package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.CommandDecorator;
import io.bootique.cli.CliFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Optionally decorates commands with the code to execute additional auxiliary commands if those are configured for
 * a give  type of command.
 *
 * @since 0.25
 */
public class ExecutionPlanBuilder {

    private Provider<CliFactory> cliFactoryProvider;
    private Provider<CommandManager> commandManagerProvider;
    private Provider<ExecutorService> executorProvider;
    private Map<Class<? extends Command>, CommandDecorator> commandDecorators;

    public ExecutionPlanBuilder(
            Provider<CliFactory> cliFactoryProvider,
            Provider<CommandManager> commandManagerProvider,
            Provider<ExecutorService> executorProvider,
            Map<Class<? extends Command>, CommandDecorator> commandDecorators) {

        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
        this.commandDecorators = commandDecorators;
    }

    /**
     * Optionally decorates provided command to execute additional auxiliary commands if those are configured for
     * this type of command.
     *
     * @param mainCommand the primary command whose decoration was requested.
     * @return either main command or a decorated main command.
     */
    public Command prepareForExecution(Command mainCommand) {

        if (commandDecorators.isEmpty()) {
            return mainCommand;
        }

        CommandDecorator decorator = commandDecorators.get(mainCommand.getClass());
        if (decorator == null) {
            return mainCommand;
        }

        return new MultiCommand(
                mainCommand,
                cliFactoryProvider,
                commandManagerProvider,
                executorProvider,
                decorator.getBefore(),
                decorator.getParallel());
    }

}
