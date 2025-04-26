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

import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;
import io.bootique.cli.NoArgsCli;
import io.bootique.command.CommandManager;
import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.OptionMetadata;
import jakarta.inject.Provider;
import joptsimple.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoptCliFactory implements CliFactory {

    private final Object optionParserLock;
    private final Provider<CommandManager> commandManagerProvider;
    private final ApplicationMetadata application;

    private volatile OptionParser optionParser;

    public JoptCliFactory(Provider<CommandManager> commandManagerProvider, ApplicationMetadata application) {

        // injecting CommandManager via provider for an obscure reason - it is injected here and also in
        // ApplicationMetadata provider (and this class depends on ApplicationMetadata). So when there's an error
        // during CommandManager construction, it is thrown twice, causing ProvisionException to lose its "cause",
        // complicating exception analysis.

        this.commandManagerProvider = commandManagerProvider;
        this.application = application;
        this.optionParserLock = new Object();
    }

    @Override
    public Cli createCli(String[] args) {
        if (args.length == 0) {
            return NoArgsCli.getInstance();
        }

        OptionSet parsed = parse(args);
        String commandName = commandName(parsed);
        return new JoptCli(parsed, commandName);
    }

    private OptionSet parse(String[] args) {
        try {
            return getParser().parse(args);
        } catch (OptionException e) {
            throw new BootiqueException(1, e.getMessage(), e);
        }
    }

    private OptionParser getParser() {
        if (optionParser == null) {
            synchronized (optionParserLock) {
                if (optionParser == null) {
                    optionParser = createParser();
                }
            }
        }
        return optionParser;
    }

    protected OptionParser createParser() {

        // do not allow option abbreviations, we will provide short forms explicitly
        OptionParser parser = new OptionParser(false);

        application.getCliOptions().forEach(o -> addOption(parser, o));
        return parser;
    }

    protected void addOption(OptionParser parser, OptionMetadata option) {

        String description = option.getDescription() != null ? option.getDescription() : "";
        List<String> longAndShort = option.getShortName() != null
                ? List.of(option.getShortName(), option.getName())
                : List.of(option.getName());

        OptionSpecBuilder optionBuilder = parser.acceptsAll(longAndShort, description);
        switch (option.getValueCardinality()) {
            case OPTIONAL:
                ArgumentAcceptingOptionSpec<String> optionSpec = optionBuilder.withOptionalArg().describedAs(option.getValueName());
                if (option.getDefaultValue() != null) {
                    optionSpec.defaultsTo(option.getDefaultValue());
                }
                break;
            case REQUIRED:
                optionBuilder.withRequiredArg().describedAs(option.getValueName());
                break;
            default:
                break;
        }
    }

    protected String commandName(OptionSet optionSet) {

        Set<String> matches = new HashSet<>(3);
        commandManagerProvider.get().getAllCommands().forEach((name, mc) -> {
            if (!mc.isHidden() && !mc.isDefault() && optionSet.has(name)) {
                matches.add(name);
            }
        });

        return switch (matches.size()) {
            // default command should be invoked
            case 0 -> null;
            case 1 -> matches.iterator().next();
            default -> {
                String opts = String.join(", ", matches);
                throw new BootiqueException(1, String.format("CLI options match multiple commands: %s.", opts));
            }
        };
    }
}
