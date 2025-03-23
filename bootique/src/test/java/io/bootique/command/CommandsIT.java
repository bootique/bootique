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

import io.bootique.BQModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cli.Cli;
import io.bootique.di.DIRuntimeException;
import io.bootique.help.HelpCommand;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class CommandsIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    private static void assertCommandKeys(Map<String, ManagedCommand> commands, String... expectedCommands) {
        assertEquals(commands.size(), expectedCommands.length);
        assertEquals(new HashSet<>(asList(expectedCommands)), commands.keySet());
    }

    @Test
    public void moduleCommands() {
        BQModule commandsModule = Commands.builder().module();
        BQRuntime runtime = appManager.runtime(Bootique.app().module(commandsModule));
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
    public void noModuleCommands() {
        BQModule commandsModule = Commands.builder().noModuleCommands().module();
        BQRuntime runtime = appManager.runtime(Bootique.app().module(commandsModule));
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "help", "help-config");

        ManagedCommand help = commands.get("help");
        assertTrue(help.isHidden());
        assertFalse(help.isDefault());
        assertTrue(help.isHelp());

        ManagedCommand helpConfig = commands.get("help-config");
        assertTrue(helpConfig.isHidden());
        assertFalse(helpConfig.isDefault());
    }

    @Test
    public void noModuleCommands_TryExec() {
        BQModule commandsModule = Commands.builder().noModuleCommands().module();

        BQRuntime tryHelp = appManager.runtime(Bootique.app("--help").module(commandsModule));
        assertThrows(DIRuntimeException.class, () -> tryHelp.run());

        BQRuntime tryHelpConfig = appManager.runtime(Bootique.app("--help-config").module(commandsModule));
        assertThrows(DIRuntimeException.class, () -> tryHelpConfig.run());
    }

    @Test
    public void noModuleCommands_ReaddHidden_TryExec() {
        BQModule commandsModule = Commands.builder().noModuleCommands()
                .add(HelpCommand.class)
                .module();

        BQRuntime tryHelp = appManager.runtime(Bootique.app("--help").module(commandsModule));
        CommandOutcome helpOutcome = tryHelp.run();
        assertTrue(helpOutcome.isSuccess());

        BQRuntime tryHelpConfig = appManager.runtime(Bootique.app("--help-config").module(commandsModule));
        assertThrows(DIRuntimeException.class, () -> tryHelpConfig.run());
    }

    @Test
    public void module_ExtraCommandAsType() {
        BQModule commandsModule = Commands.builder(C1.class).module();
        BQRuntime runtime = appManager.runtime(Bootique.app().module(commandsModule));
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
    public void module_ExtraCommandAsInstance() {
        BQModule commandsModule = Commands.builder().add(new C1()).module();
        BQRuntime runtime = appManager.runtime(Bootique.app().module(commandsModule));
        CommandManager commandManager = runtime.getInstance(CommandManager.class);

        Map<String, ManagedCommand> commands = commandManager.getAllCommands();
        assertCommandKeys(commands, "c1", "help", "help-config");
    }

    @Test
    public void module_ExtraCommandOverride() {
        BQModule commandsModule = Commands.builder().add(C2_Help.class).module();
        BQRuntime runtime = appManager.runtime(Bootique.app().module(commandsModule));
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
