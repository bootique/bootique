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

package io.bootique.jopt;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.DefaultCommandManager;
import io.bootique.command.ManagedCommand;
import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoptCliFactoryCommandNameTest {

    private Map<String, ManagedCommand> commands;

    @BeforeEach
    public void before() {
        this.commands = new HashMap<>();
    }

    @Test
    public void commandName_NoMatch() {

        addMockCommand("c1", "me", "them");

        Cli cli = createCli("--me");
        assertNull(cli.commandName());
    }

    @Test
    public void commandName_Match() {

        addMockCommand("c1", "me", "them");
        addMockCommand("c2", "us", "others");

        Cli cli = createCli("--me --c1");
        assertEquals("c1", cli.commandName());
    }

    @Test
    public void commandName_MultipleMatches() {

        addMockCommand("c1", "me", "them");
        addMockCommand("c2", "us", "others");

        assertThrows(RuntimeException.class, () -> createCli("--me --c1 --c2"));
    }

    private void addMockCommand(String name, String... options) {

        Command mock = mock(Command.class);

        // for now JopCLiProvider adds command name as an option;
        // using this option in command line would match the original command
        // name

        CommandMetadata.Builder builder = CommandMetadata.builder(name);
        Arrays.asList(options).forEach(opt -> builder.addOption(OptionMetadata.builder(opt)));

        CommandMetadata md = builder.build();

        when(mock.getMetadata()).thenReturn(md);

        ManagedCommand managedCommand  = ManagedCommand.builder(mock).build();

        commands.put(mock.getMetadata().getName(), managedCommand);
    }

    private Cli createCli(String args) {
        String[] argsArray = args.split(" ");

        CommandManager commandManager = new DefaultCommandManager(commands);

        ApplicationMetadata.Builder appBuilder = ApplicationMetadata.builder();
        commands.values().forEach(c -> appBuilder.addCommand(c.getCommand().getMetadata()));

        return new JoptCliFactory(() -> commandManager, appBuilder.build()).createCli(argsArray);
    }

}
