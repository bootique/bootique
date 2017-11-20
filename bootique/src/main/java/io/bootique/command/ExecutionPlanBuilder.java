package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.cli.CliFactory;
import io.bootique.log.BootLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Optionally decorates commands with the code to execute additional auxiliary commands if those are configured for
 * a given type of command.
 *
 * @since 0.25
 */
public class ExecutionPlanBuilder {

    private BootLogger logger;
    private Provider<CliFactory> cliFactoryProvider;
    private Provider<CommandManager> commandManagerProvider;
    private Provider<ExecutorService> executorProvider;
    private Map<Class<? extends Command>, CommandDecorator> decorators;

    public ExecutionPlanBuilder(
            Provider<CliFactory> cliFactoryProvider,
            Provider<CommandManager> commandManagerProvider,
            Provider<ExecutorService> executorProvider,
            Map<Class<? extends Command>, CommandDecorator> decorators,
            BootLogger logger) {

        this.logger = logger;
        this.decorators = decorators;
        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
    }

    public static Map<Class<? extends Command>, CommandDecorator> mergeDecorators(
            Set<CommandRefWithDecorator> decoratorSet) {


        if (decoratorSet.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Class<? extends Command>, CommandDecorator.Builder> mergedMutable
                = new HashMap<>((int) (decoratorSet.size() / 0.75));

        decoratorSet.forEach(ref -> {
            mergedMutable.computeIfAbsent(ref.getCommandType(), c -> CommandDecorator.builder())
                    .copyFrom(ref.getDecorator());
        });

        Map<Class<? extends Command>, CommandDecorator> merged = new HashMap<>();
        mergedMutable.forEach((k, v) -> merged.put(k, v.build()));
        return merged;
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
                executorProvider,
                logger);
    }

}
