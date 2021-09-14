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

package io.bootique.help.config;

import javax.inject.Provider;

import io.bootique.meta.MetadataNode;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;

public class HelpConfigCommand extends CommandWithMetadata {

    private BootLogger bootLogger;
    private Provider<ConfigHelpGenerator> helpGeneratorProvider;

    public HelpConfigCommand(BootLogger bootLogger, Provider<ConfigHelpGenerator> helpGeneratorProvider) {
        super(CommandMetadata
                .builder(HelpConfigCommand.class)
                .description("Prints information about application modules and their configuration options.")
                .shortName('H'));

        this.bootLogger = bootLogger;
        this.helpGeneratorProvider = helpGeneratorProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        Set<String> arguments = new HashSet<>(cli.standaloneArguments());

        Predicate<MetadataNode> predicate = (arguments.size() == 0)
                ? o -> true
                : o -> arguments.contains(o.getName());

        StringBuilder out = new StringBuilder();
        helpGeneratorProvider.get().append(out, predicate);
        bootLogger.stdout(out.toString());
        return CommandOutcome.succeeded();
    }
}
