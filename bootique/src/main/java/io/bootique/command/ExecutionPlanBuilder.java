/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
            Set<CommandRefDecorated> decoratorSet) {


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
