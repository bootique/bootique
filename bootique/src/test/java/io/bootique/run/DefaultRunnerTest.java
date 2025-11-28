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

package io.bootique.run;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandOutcome;
import io.bootique.command.DefaultCommandManager;
import io.bootique.command.ExecutionPlanBuilder;
import io.bootique.command.ManagedCommand;
import io.bootique.log.DefaultBootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import joptsimple.OptionSpec;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultRunnerTest {

    @Test
    public void run() {

        TestCommand dflt = TestCommand.of("d1");
        TestCommand help = TestCommand.of("h1");
        TestCommand c1 = TestCommand.of("c1", "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        CommandOutcome outcome = run("c1", dflt, help, c1, c2);
        assertTrue(outcome.isSuccess());

        assertFalse(dflt.wasRun);
        assertFalse(help.wasRun);
        assertTrue(c1.wasRun);
        assertFalse(c2.wasRun);
    }

    @Test
    public void run_ReverseOrder() {

        TestCommand dflt = TestCommand.of("d1");
        TestCommand help = TestCommand.of("h1");
        TestCommand c1 = TestCommand.of("c1", "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        CommandOutcome outcome = run("c2", dflt, help, c1, c2);
        assertTrue(outcome.isSuccess());

        assertFalse(dflt.wasRun);
        assertFalse(help.wasRun);
        assertFalse(c1.wasRun);
        assertTrue(c2.wasRun);
    }

    @Test
    public void run_NoMatch() {
        TestCommand dflt = TestCommand.of("d1");
        TestCommand help = TestCommand.of("h1");
        TestCommand c1 = TestCommand.of("c1", "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        assertThrows(IllegalStateException.class, () -> run("c3", dflt, help, c1, c2));
    }

    @Test
    public void run_NullName_Default() {

        TestCommand dflt = TestCommand.of("d1");
        TestCommand help = TestCommand.of("h1");
        TestCommand c1 = TestCommand.of("c1", "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        CommandOutcome outcome = run(null, dflt, help, c1, c2);
        assertTrue(outcome.isSuccess());

        assertTrue(dflt.wasRun);
        assertFalse(help.wasRun);
        assertFalse(c1.wasRun);
        assertFalse(c2.wasRun);
    }

    @Test
    public void run_NullName_Help() {

        TestCommand help = TestCommand.of("h1");
        TestCommand c1 = TestCommand.of("c1", "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        CommandOutcome outcome = run(null, null, help, c1, c2);
        assertTrue(outcome.isSuccess());

        assertTrue(help.wasRun);
        assertFalse(c1.wasRun);
        assertFalse(c2.wasRun);
    }

    @Test
    public void run_NullName_NoFallback() {

        TestCommand c1 = TestCommand.of("c1", "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        CommandOutcome outcome = run(null, null, null, c1, c2);
        assertTrue(outcome.isSuccess());

        assertFalse(c1.wasRun);
        assertFalse(c2.wasRun);
    }

    @Test
    public void run_Failure() {

        TestCommand c1 = TestCommand.of("c1", CommandOutcome.failed(-1, "fff"), "c1o1", "c1o2");
        TestCommand c2 = TestCommand.of("c2", "c2o1");

        CommandOutcome outcome = run("c1", null, null, c1, c2);
        assertFalse(outcome.isSuccess());
        assertEquals(-1, outcome.getExitCode());
        assertEquals("fff", outcome.getMessage());
    }

    private CommandOutcome run(String cliCommand, Command defaultCommand, Command helpCommand, Command... commands) {

        Map<String, ManagedCommand> commandMap = new HashMap<>();
        asList(commands).forEach(c -> commandMap.put(c.getMetadata().getName(), ManagedCommand.forCommand(c)));

        if (defaultCommand != null) {
            ManagedCommand mc = ManagedCommand.builder(defaultCommand).asDefault().build();
            commandMap.put(defaultCommand.getMetadata().getName(), mc);
        }

        if (helpCommand != null) {
            ManagedCommand mc = ManagedCommand.builder(helpCommand).asHelp().build();
            commandMap.put(helpCommand.getMetadata().getName(), mc);
        }

        CommandManager commandManager = new DefaultCommandManager(commandMap);
        Cli cli = cli(cliCommand);

        ExecutionPlanBuilder planBuilder = new ExecutionPlanBuilder(
                () -> args -> cli,
                () -> commandManager,
                () -> null,
                Map.of(),
                new DefaultBootLogger(false)
        ) {
            @Override
            public Command prepareForExecution(Command mainCommand) {
                return mainCommand;
            }
        };

        return new DefaultRunner(cli, commandManager, planBuilder).run();
    }

    static class TestCommand implements Command {

        private final CommandMetadata metadata;
        private final CommandOutcome outcome;
        boolean wasRun;

        public static TestCommand of(String name, String... options) {
            return new TestCommand(name, CommandOutcome.succeeded(), options);
        }

        public static TestCommand of(String name, CommandOutcome outcome, String... options) {
            return new TestCommand(name, outcome, options);
        }

        TestCommand(String name, CommandOutcome outcome, String... options) {
            CommandMetadata.Builder builder = CommandMetadata.builder(name);
            List.of(options).forEach(opt -> builder.addOption(OptionMetadata.builder(opt).build()));
            this.metadata = builder.build();
            this.outcome = outcome;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            this.wasRun = true;
            return outcome;
        }

        @Override
        public CommandMetadata getMetadata() {
            return metadata;
        }
    }

    private static Cli cli(String command) {

        return new Cli() {
            @Override
            public String commandName() {
                return command;
            }

            @Override
            public boolean hasOption(String name) {
                return false;
            }

            @Override
            public List<OptionSpec<?>> detectedOptions() {
                return List.of();
            }

            @Override
            public List<String> optionStrings(String name) {
                return List.of();
            }

            @Override
            public List<String> standaloneArguments() {
                return List.of();
            }
        };
    }
}
