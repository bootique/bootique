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
package io.bootique.meta.application;

import io.bootique.command.CommandManager;
import io.bootique.env.DeclaredVariable;
import io.bootique.log.BootLogger;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Set;

/**
 * @since 3.0
 */
public class ApplicationMetadataFactory {

    public static ApplicationMetadata of(
            BootLogger logger,
            String description,
            CommandManager commandManager,
            Set<OptionMetadata> options,
            Set<DeclaredVariable> declaredVars,
            ModulesMetadata modulesMetadata) {

        ApplicationMetadata.Builder builder = ApplicationMetadata
                .builder()
                .description(description)
                .addOptions(options);

        commandManager
                .getAllCommands()
                .values()
                .stream()
                .filter(mc -> !mc.isHidden() && !mc.isDefault())
                .forEach(mc -> builder.addCommand(mc.getCommand().getMetadata()));

        // merge default command options with top-level app options
        commandManager.getPublicDefaultCommand().ifPresent(c -> builder.addOptions(c.getMetadata().getOptions()));

        declaredVars.forEach(dv -> {
            ConfigValueMetadata varMd = DeclaredVariableMetaCompiler.compile(dv, modulesMetadata);
            if (varMd.isUnbound()) {
                logger.trace(() ->
                        "Can't reliably determine whether the path '"
                                + dv.getConfigPath()
                                + "' linked to the env var '"
                                + varMd.getName()
                                + "' is valid. This is likely not an error.");
            }
            builder.addVariable(varMd);
        });

        return builder.build();
    }
}
