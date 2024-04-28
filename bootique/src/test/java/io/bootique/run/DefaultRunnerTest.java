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
import io.bootique.command.*;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DefaultRunnerTest {

    private Cli mockCli;

    private static Command mockCommand(String name, CommandOutcome outcome, String... options) {

        CommandMetadata.Builder builder = CommandMetadata.builder(name);
        List.of(options).forEach(opt -> builder.addOption(OptionMetadata.builder(opt).build()));
        CommandMetadata md = builder.build();

        Command mock = mock(Command.class);
        when(mock.run(any())).thenReturn(outcome);
        when(mock.getMetadata()).thenReturn(md);

        return mock;
    }

    @BeforeEach
    public void before() {
        this.mockCli = mock(Cli.class);
    }

    @Test
    public void run() {

        when(mockCli.commandName()).thenReturn("c1");

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
        verify(mockDefault, times(0)).run(mockCli);
        verify(mockHelp, times(0)).run(mockCli);
    }

    @Test
    public void run_ReverseOrder() {

        when(mockCli.commandName()).thenReturn("c2");

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC2).run(mockCli);
        verify(mockC1, times(0)).run(mockCli);
        verify(mockDefault, times(0)).run(mockCli);
        verify(mockHelp, times(0)).run(mockCli);
    }

    @Test
    public void run_NoMatch() {

        when(mockCli.commandName()).thenReturn("c3");

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        assertThrows(IllegalStateException.class, () -> run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2));
    }

    @Test
    public void run_NullName_Default() {

        when(mockCli.commandName()).thenReturn(null);

        Command mockDefault = mockCommand("d1", CommandOutcome.succeeded());
        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.of(mockDefault), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1, times(0)).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
        verify(mockDefault).run(mockCli);
        verify(mockHelp, times(0)).run(mockCli);
    }

    @Test
    public void run_NullName_Help() {

        when(mockCli.commandName()).thenReturn(null);

        Command mockHelp = mockCommand("h1", CommandOutcome.succeeded());

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.empty(), Optional.of(mockHelp), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1, times(0)).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
        verify(mockHelp).run(mockCli);
    }

    @Test
    public void run_NullName_NoFallback() {

        when(mockCli.commandName()).thenReturn(null);

        Command mockC1 = mockCommand("c1", CommandOutcome.succeeded(), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.empty(), Optional.empty(), mockC1, mockC2);
        assertTrue(result.isSuccess());

        verify(mockC1, times(0)).run(mockCli);
        verify(mockC2, times(0)).run(mockCli);
    }

    @Test
    public void run_Failure() {

        when(mockCli.commandName()).thenReturn("c1");

        Command mockC1 = mockCommand("c1", CommandOutcome.failed(-1, "fff"), "c1o1", "c1o2");
        Command mockC2 = mockCommand("c2", CommandOutcome.succeeded(), "c2o1");

        CommandOutcome result = run(Optional.empty(), Optional.empty(), mockC1, mockC2);
        assertFalse(result.isSuccess());
        assertEquals(-1, result.getExitCode());
        assertEquals("fff", result.getMessage());
    }

    private CommandOutcome run(Optional<Command> defaultCommand, Optional<Command> helpCommand, Command... commands) {

        Map<String, ManagedCommand> commandMap = new HashMap<>();
        asList(commands).forEach(c -> commandMap.put(c.getMetadata().getName(), ManagedCommand.forCommand(c)));

        defaultCommand.ifPresent(dc -> {
            ManagedCommand mc = ManagedCommand.builder(dc).asDefault().build();
            commandMap.put(dc.getMetadata().getName(), mc);
        });

        helpCommand.ifPresent(hc -> {
            ManagedCommand mc = ManagedCommand.builder(hc).asHelp().build();
            commandMap.put(hc.getMetadata().getName(), mc);
        });

        CommandManager commandManager = new DefaultCommandManager(commandMap);
        ExecutionPlanBuilder executionPlanBuilder = mock(ExecutionPlanBuilder.class);
        when(executionPlanBuilder.prepareForExecution(any(Command.class))).thenAnswer(i -> i.getArguments()[0]);

        return new DefaultRunner(mockCli, commandManager, executionPlanBuilder).run();
    }
}
