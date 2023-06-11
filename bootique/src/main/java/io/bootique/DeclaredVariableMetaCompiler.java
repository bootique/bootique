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

package io.bootique;

import io.bootique.env.DeclaredVariable;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Optional;

class DeclaredVariableMetaCompiler {

    static ConfigValueMetadata compile(DeclaredVariable var, ModulesMetadata modulesMetadata) {

        for (ModuleMetadata mm : modulesMetadata.getModules()) {
            // TODO: 'findConfig' does a String split over and over again as we iterate through the loop.
            //  Precalculate this once.
            Optional<ConfigMetadataNode> cmn = mm.findConfig(var.getConfigPath());
            if (cmn.isPresent()) {
                return compileMetadata(var, cmn.get());
            }
        }

        return compileUnboundMetadata(var);
    }

    private static ConfigValueMetadata compileUnboundMetadata(DeclaredVariable variable) {
        return ConfigValueMetadata
                .builder(variable.getName())
                .unbound()
                .description(variable.getDescription() != null ? variable.getDescription() : null)
                .build();
    }

    private static ConfigValueMetadata compileMetadata(DeclaredVariable variable, ConfigMetadataNode configMetadata) {

        // TODO: validation... verify that the variable is bound to a value, not a collection or a map??

        return ConfigValueMetadata
                .builder(variable.getName())
                .description(variable.getDescription() != null ? variable.getDescription() : configMetadata.getDescription())
                .type(configMetadata.getType())
                .build();
    }
}
