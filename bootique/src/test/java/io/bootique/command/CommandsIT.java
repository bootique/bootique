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

import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class CommandsIT {

    @RegisterExtension
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private String[] args;

    private static void assertCommandKeys(Map<String, ManagedCommand> commands, String... expectedCommands) {
        assertEquals(commands.size(), expectedCommands.length);
        assertEquals(new HashSet<>(asList(expectedCommands)), commands.keySet());
    }

    @BeforeEach
    public void before() {
        args = new String[]{"a", "b", "c"};
    }

    @Test
    public void testModuleCommands() {
        BQModuleProvider provider = Commands.builder().build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");

        assertFalse(commands.get("help").isHidden());
        assertFalse(commands.get("help").isDefault());
        assertTrue(commands.get("help").isHelp());

        assertFalse(commands.get("help-config").isHidden());
        assertFalse(commands.get("help-config").isDefault());
    }

    @Test
    public void testNoModuleCommands() {
        BQModuleProvider provider = Commands.builder().noModuleCommands().build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");

        assertTrue(commands.get("help").isHidden());
        assertFalse(commands.get("help").isDefault());
        assertTrue(commands.get("help").isHelp());

        assertTrue(commands.get("help-config").isHidden());
        assertFalse(commands.get("help-config").isDefault());
    }

    @Test
    public void testModule_ExtraCommandAsType() {
        BQModuleProvider provider = Commands.builder(C1.class).build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "c1", "help", "help-config");

        assertFalse(commands.get("help").isHidden());
        assertFalse(commands.get("help").isDefault());
        assertTrue(commands.get("help").isHelp());

        assertFalse(commands.get("help-config").isHidden());
        assertFalse(commands.get("help-config").isDefault());

        assertTrue(commands.containsKey("c1"));
        assertFalse(commands.get("c1").isDefault());
    }

    @Test
    public void testModule_ExtraCommandAsInstance() {
        BQModuleProvider provider = Commands.builder().add(new C1()).build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "c1", "help", "help-config");
    }

    @Test
    public void testModule_ExtraCommandOverride() {
        BQModuleProvider provider = Commands.builder().add(C2_Help.class).build();
        BQRuntime runtime = testFactory.app(args).module(provider).createRuntime();
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");
    }

    static class C1 implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class C2_Help implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }

        @Override
        public CommandMetadata getMetadata() {
            return CommandMetadata.builder("help").build();
        }
    }
}
