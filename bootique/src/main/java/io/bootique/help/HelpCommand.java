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

package io.bootique.help;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import jakarta.inject.Provider;

/**
 * A built-in command that prints dynamically-create application help in a format similar to UNIX man pages.
 */
public class HelpCommand extends CommandWithMetadata {

    private final BootLogger bootLogger;
    private final Provider<HelpGenerator> helpGeneratorProvider;

    public HelpCommand(BootLogger bootLogger, Provider<HelpGenerator> helpGeneratorProvider) {
        super(CommandMetadata.builder(HelpCommand.class).description("Prints this message.").build());
        this.bootLogger = bootLogger;
        this.helpGeneratorProvider = helpGeneratorProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        return printHelp(cli);
    }

    protected CommandOutcome printHelp(Cli cli) {

        StringBuilder out = new StringBuilder();
        helpGeneratorProvider.get().append(out);

        bootLogger.stdout(out.toString());
        return CommandOutcome.succeeded();
    }

}
