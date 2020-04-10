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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bootique.di.BQModule;
import io.bootique.log.BootLogger;

class RuntimeModuleMerger {

    private BootLogger bootLogger;

    RuntimeModuleMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    Collection<BQModule> toDIModules(Collection<BQModuleMetadata> bqModulesMetadata) {
        ModuleGraph moduleGraph = new ModuleGraph(bqModulesMetadata.size());
        Map<Class<? extends BQModule>, BQModuleMetadata> moduleByClass = new HashMap<>();
        bqModulesMetadata.forEach(metadata -> {
            Class<? extends BQModule> moduleClass = metadata.getModule().getClass();
            moduleByClass.putIfAbsent(moduleClass, metadata);
            moduleGraph.add(metadata);
            if(metadata.getOverrides().isEmpty()) {
                bootLogger.trace(() -> traceMessage(metadata, null));
            }
        });
        bqModulesMetadata.forEach(metadata -> metadata.getOverrides()
                .forEach(override -> {
                    BQModuleMetadata overrideModule = moduleByClass.get(override);
                    moduleGraph.add(metadata, overrideModule);
                    bootLogger.trace(() -> traceMessage(metadata, overrideModule));
                }));

        List<BQModule> modules = new ArrayList<>(moduleByClass.size());
        moduleGraph.topSort().forEach(moduleClass -> modules.add(moduleClass.getModule()));
        return modules;
    }

    private String traceMessage(BQModuleMetadata module, BQModuleMetadata overriddenBy) {
        StringBuilder message = new StringBuilder("Loading module '")
                .append(module.getName())
                .append("'");

        String providerName = module.getProviderName();
        boolean hasProvider = providerName != null && providerName.length() > 0;
        if (hasProvider) {
            message.append(" provided by '").append(providerName).append("'");
        }

        if (overriddenBy != null) {
            if (hasProvider) {
                message.append(",");
            }

            message.append(" overridden by '").append(overriddenBy.getName()).append("'");
        }

        return message.toString();
    }
}
