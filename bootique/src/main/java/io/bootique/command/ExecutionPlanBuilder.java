package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.cli.CliFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Optionally decorates commands with the code to execute additional auxiliary commands if those are configured for
 * a given type of command.
 *
 * @since 0.25
 */
public class ExecutionPlanBuilder {

    private Provider<CliFactory> cliFactoryProvider;
    private Provider<CommandManager> commandManagerProvider;
    private Provider<ExecutorService> executorProvider;
    private Map<Class<? extends Command>, CommandDecorator> decorators;

    public ExecutionPlanBuilder(
            Provider<CliFactory> cliFactoryProvider,
            Provider<CommandManager> commandManagerProvider,
            Provider<ExecutorService> executorProvider,
            Map<Class<? extends Command>, CommandDecorator> decorators) {

        this.decorators = decorators;
        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
    }

    /**
     * Optionally decorates provided command to execute additional auxiliary commands if those are configured for
     * this type of command.
     *
     * @param mainCommand the primary command whose execution plan was requested.
     * @return either main command or a decorated main command.
     */
    public Command prepareForExecution(Command mainCommand) {

        if (decorators.isEmpty()) {
            return mainCommand;
        }

        CommandDecorator decorator = decorators.get(mainCommand.getClass());
        if (decorator == null) {
            return mainCommand;
        }

        return new MultiCommand(
                mainCommand,
                decorator,
                cliFactoryProvider,
                commandManagerProvider,
                executorProvider);
    }

}
