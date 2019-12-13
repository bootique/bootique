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

import io.bootique.di.BQModule;
import io.bootique.log.BootLogger;

import java.util.*;

class RuntimeModuleMerger {

    private BootLogger bootLogger;

    RuntimeModuleMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    Collection<BQModule> toDIModules(Collection<BQModuleMetadata> bqModules) {
        ModuleGraph moduleGraph = new ModuleGraph();
        Map<Class<? extends BQModule>, BQModuleMetadata> moduleByClass = new HashMap<>();
        bqModules.forEach(bqModule -> {
            Class<? extends BQModule> moduleClass = bqModule.getModule().getClass();
            moduleByClass.putIfAbsent(moduleClass, bqModule);
            moduleGraph.add(bqModule);
            if(bqModule.getOverrides().isEmpty()) {
                bootLogger.trace(() -> traceMessage(bqModule, null));
            }
        });
        bqModules.forEach(bqModule -> bqModule.getOverrides()
                .forEach(override -> {
                    BQModuleMetadata overrideModule = moduleByClass.get(override);
                    moduleGraph.add(bqModule, overrideModule);
                    bootLogger.trace(() -> traceMessage(bqModule, overrideModule));
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
