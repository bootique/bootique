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

import io.bootique.cli.Cli;
import io.bootique.config.jackson.merger.JsonConfigurationMerger;
import io.bootique.config.jackson.parser.JsonConfigurationParser;
import io.bootique.log.BootLogger;

import javax.inject.Inject;

/**
 * Loads and merges configuration passed via CLI "--config" option.
 *
 * @since 2.0
 */
public class CliConfigurationLoader extends UrlConfigurationLoader {

    public static final String CONFIG_OPTION = "config";
    public static final int ORDER = DIConfigurationLoader.ORDER + 10;

    @Inject
    public CliConfigurationLoader(
            BootLogger bootLogger,
            JsonConfigurationParser parser,
            JsonConfigurationMerger merger,
            Cli cli) {
        super(bootLogger, parser, merger, cli.optionStrings(CONFIG_OPTION));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
