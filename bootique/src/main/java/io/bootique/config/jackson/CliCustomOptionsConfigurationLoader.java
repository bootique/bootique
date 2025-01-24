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
package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.cli.Cli;
import io.bootique.config.OptionRefWithConfig;
import io.bootique.config.OptionRefWithConfigPath;
import io.bootique.config.jackson.merger.InPlacePropertiesMerger;
import io.bootique.config.jackson.merger.JsonConfigurationMerger;
import io.bootique.config.jackson.parser.JsonConfigurationParser;
import io.bootique.meta.application.OptionMetadata;
import jakarta.inject.Inject;
import joptsimple.OptionSpec;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Loads and merges configuration passed via custom CLI options other than "--config". Custom CLI options can
 * be linked to configuration paths or configuration URLs.
 *
 * @since 2.0
 */
public class CliCustomOptionsConfigurationLoader implements JsonConfigurationLoader {

    public static final int ORDER = CliConfigurationLoader.ORDER + 10;

    private final Cli cli;
    private final Set<OptionMetadata> optionMetadata;
    private final Set<OptionRefWithConfig> optionDecorators;
    private final Set<OptionRefWithConfigPath> optionPathDecorators;
    private final JsonConfigurationParser parser;
    private final JsonConfigurationMerger merger;

    @Inject
    public CliCustomOptionsConfigurationLoader(
            Cli cli,
            Set<OptionMetadata> optionMetadata,
            Set<OptionRefWithConfig> optionDecorators,
            Set<OptionRefWithConfigPath> optionPathDecorators,
            JsonConfigurationParser parser,
            JsonConfigurationMerger merger) {

        this.cli = cli;
        this.optionMetadata = optionMetadata;
        this.optionDecorators = optionDecorators;
        this.optionPathDecorators = optionPathDecorators;
        this.parser = parser;
        this.merger = merger;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public JsonNode updateConfiguration(JsonNode mutableInput) {
        if (optionMetadata.isEmpty()) {
            return mutableInput;
        }

        List<OptionSpec<?>> detectedOptions = cli.detectedOptions();
        if (detectedOptions.isEmpty()) {
            return mutableInput;
        }

        for (OptionSpec<?> cliOpt : detectedOptions) {

            OptionMetadata omd = findMetadata(cliOpt);
            if (omd == null) {
                continue;
            }

            // config decorators are loaded first, and then can be overridden from options...
            for (OptionRefWithConfig decorator : optionDecorators) {
                if (decorator.getOptionName().equals(omd.getName())) {
                    JsonNode parsed = parser.parse(decorator.getConfigResource().getUrl());
                    if (parsed != null) {
                        mutableInput = merger.apply(mutableInput, parsed);
                    }
                }
            }

            for (OptionRefWithConfigPath pathDecorator : optionPathDecorators) {
                if (pathDecorator.getOptionName().equals(omd.getName())) {
                    String cliValue = cli.optionString(omd.getName());
                    mutableInput = new InPlacePropertiesMerger(
                            Collections.singletonMap(pathDecorator.getConfigPath(), cliValue)).apply(mutableInput);
                }
            }
        }

        return mutableInput;
    }

    private OptionMetadata findMetadata(OptionSpec<?> option) {

        List<String> optionNames = option.options();

        // TODO: allow lookup of option metadata by name to avoid linear scans...
        // Though we are dealing with small collection, so shouldn't be too horrible.

        for (OptionMetadata omd : optionMetadata) {
            if (optionNames.contains(omd.getName())) {
                return omd;
            }
        }

        // this was likely a command, not an option.
        return null;
    }
}
