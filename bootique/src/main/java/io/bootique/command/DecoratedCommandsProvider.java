package io.bootique.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.BootiqueException;
import io.bootique.CommandDecorator;
import io.bootique.cli.CliFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DecoratedCommandsProvider implements Provider<Set<Command>> {

    private Provider<CliFactory> cliFactoryProvider;
    private Provider<CommandManager> commandManagerProvider;
    private Provider<ExecutorService> executorProvider;
    private Set<Command> commands;
    private Map<Class<? extends Command>, CommandDecorator> commandDecorators;

    @Inject
    public DecoratedCommandsProvider(Provider<CliFactory> cliFactoryProvider,
                                     Provider<CommandManager> commandManagerProvider,
                                     @CommandExecutor Provider<ExecutorService> executorProvider,
                                     Set<Command> commands,
                                     Map<Class<? extends Command>, CommandDecorator> commandDecorators) {
        this.cliFactoryProvider = cliFactoryProvider;
        this.commandManagerProvider = commandManagerProvider;
        this.executorProvider = executorProvider;
        this.commands = commands;
        this.commandDecorators = commandDecorators;
    }

    @Override
    public Set<Command> get() {
        if (commandDecorators.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Command> decoratedCommands = new HashSet<>();

        Map<Class<? extends Command>, Command> commandMap = commandMap();
        commandDecorators.forEach((commandType, decorator) -> {
            Command originalCommand = commandMap.get(commandType);
            if (originalCommand == null) {
                throw new BootiqueException(1, "Unknown command type: " + commandType.getName());
            }

            decoratedCommands.add(new DecoratedCommand(
                    originalCommand,
                    cliFactoryProvider,
                    commandManagerProvider,
                    executorProvider,
                    decorator.getBefore(),
                    decorator.getParallel()));
        });

        return Collections.unmodifiableSet(decoratedCommands);
    }

    private Map<Class<? extends Command>, Command> commandMap() {
        return commands.stream().collect(Collectors.toMap(c -> c.getClass(), Function.identity()));
    }
}
