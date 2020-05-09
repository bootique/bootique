/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.command;

import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandDecorator_CommandsIT {

    @RegisterExtension
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testAlsoRun_DecorateWithPrivate() {

        // use private "-s" command in decorator
        BQModuleProvider commandsOverride = Commands.builder().add(MainCommand.class).noModuleCommands().build();
        CommandDecorator decorator = CommandDecorator.alsoRun("s");

        BQRuntime runtime = createRuntime(commandsOverride, decorator);
        CommandOutcome outcome = runtime.run();

        waitForAllToFinish();

        assertTrue(outcome.isSuccess());
        assertTrue(getCommand(runtime, MainCommand.class).isExecuted());
        assertTrue(getCommand(runtime, SuccessfulCommand.class).isExecuted());
    }

    @Test
    public void testBeforeRun_DecorateWithPrivate() {

        // use private "-s" command in decorator
        BQModuleProvider commandsOverride = Commands.builder().add(MainCommand.class).noModuleCommands().build();
        CommandDecorator decorator = CommandDecorator.beforeRun("s");

        BQRuntime runtime = createRuntime(commandsOverride, decorator);
        CommandOutcome outcome = runtime.run();

        assertTrue(outcome.isSuccess());
        assertTrue(getCommand(runtime, MainCommand.class).isExecuted());
        assertTrue(getCommand(runtime, SuccessfulCommand.class).isExecuted());
    }

    private BQRuntime createRuntime(BQModuleProvider commandsOverride, CommandDecorator decorator) {
        return testFactory
                .app("--a")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(MainCommand.class)
                        .addCommand(SuccessfulCommand.class)
                        .decorateCommand(MainCommand.class, decorator))
                .module(commandsOverride)
                .createRuntime();
    }

    private <T extends Command> T getCommand(BQRuntime runtime, Class<T> type) {
        return (T) runtime.getInstance(CommandManager.class).lookupByType(type).getCommand();
    }

    private void waitForAllToFinish() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MainCommand extends CommandDecoratorIT.ExecutableOnceCommand {

        private static final String NAME = "a";

        MainCommand() {
            super(NAME);
        }
    }

    private static class SuccessfulCommand extends CommandDecoratorIT.ExecutableOnceCommand {

        private static final String NAME = "s";

        SuccessfulCommand() {
            super(NAME);
        }
    }
}
