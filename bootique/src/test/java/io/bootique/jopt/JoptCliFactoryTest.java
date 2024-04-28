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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoptCliFactoryTest {

    private Map<String, ManagedCommand> commands;

    @BeforeEach
    public void before() {
        this.commands = new HashMap<>();
    }

    @Test
    public void get_HasOption() {

        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").build()));

        assertTrue(createCli("-m").hasOption("me"));
        assertTrue(createCli("--me").hasOption("me"));
        assertFalse(createCli("-m").hasOption("not_me"));
        assertTrue(createCli("-m").hasOption("m"));
    }

    @Test
    public void optionStrings_Short() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null).build()));

        assertEquals(createCli("-m v4").optionStrings("me"), "v4");
    }

    @Test
    public void optionStrings_Long_Equals() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null).build()));

        assertEquals(createCli("--me=v4").optionStrings("me"), "v4");
    }

    @Test
    public void optionStrings_Long_Space() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null).build()));

        assertEquals(createCli("--me v4").optionStrings("me"), "v4");
    }

    @Test
    public void optionStrings_Single_Mixed() {

        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null).build())
                .addOption(OptionMetadata.builder("other").valueOptional(null).build()));

        assertEquals(createCli("--other v2 --me=v4").optionStrings("me"), "v4");
    }

    @Test
    public void optionStrings_Multiple_Mixed() {
        addMockCommand(CommandMetadata.builder("c1").addOption(OptionMetadata.builder("me").valueOptional(null).build())
                .addOption(OptionMetadata.builder("other").valueOptional(null).build())
                .addOption(OptionMetadata.builder("n").valueOptional(null).build())
                .addOption(OptionMetadata.builder("yes").build()));

        assertEquals(createCli("--me=v1 --other v2 -n v3 --me v4 --yes").optionStrings("me"), "v1", "v4");
    }

    @Test
    public void standaloneArguments_Mix() {
        addMockCommand(CommandMetadata.builder("c1")
                .addOption(OptionMetadata.builder("me").valueOptional(null).build())
                .addOption(OptionMetadata.builder("other").valueOptional(null).build())
                .addOption(OptionMetadata.builder("yes").build()));

        assertEquals(createCli("a --me=v1 --other v2 b --me v4 --yes c d").standaloneArguments(), "a", "b", "c", "d");
    }

    @Test
    public void standaloneArguments_DashDash() {
        addMockCommand(CommandMetadata.builder("c1")
                .addOption(OptionMetadata.builder("me").valueOptional(null).build())
                .addOption(OptionMetadata.builder("other").valueOptional(null).build()));

        assertEquals(createCli("a --me=v1 -- --other v2").standaloneArguments(), "a", "--other", "v2");
    }

    private void assertEquals(Collection<String> result, String... expected) {
        assertArrayEquals(expected, result.toArray());
    }

    private Command addMockCommand(CommandMetadata.Builder metadataBuilder) {
        Command mock = mock(Command.class);
        when(mock.getMetadata()).thenReturn(metadataBuilder.build());
        commands.put(mock.getMetadata().getName(), ManagedCommand.builder(mock).build());
        return mock;
    }

    private Cli createCli(String args) {
        String[] argsArray = args.split(" ");

        CommandManager commandManager = new DefaultCommandManager(commands);

        ApplicationMetadata.Builder appBuilder = ApplicationMetadata.builder();
        commands.values().forEach(mc -> appBuilder.addCommand(mc.getCommand().getMetadata()));

        return new JoptCliFactory(() -> commandManager, appBuilder.build()).createCli(argsArray);
    }
}
