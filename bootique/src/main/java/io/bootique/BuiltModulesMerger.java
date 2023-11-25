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

import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;
import io.bootique.log.BootLogger;

import java.util.*;

class BuiltModulesMerger {

    private final BootLogger bootLogger;

    BuiltModulesMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    Collection<BQModule> toDIModules(Collection<BuiltModule> builtModules) {
        ModuleGraph moduleGraph = new ModuleGraph(builtModules.size());
        Map<Class<? extends BQModule>, BuiltModule> moduleByClass = new HashMap<>();

        for (BuiltModule bm : builtModules) {
            Class<? extends BQModule> moduleClass = bm.getModule().getClass();
            moduleByClass.putIfAbsent(moduleClass, bm);
            moduleGraph.add(bm);

            if (bm.getOverrides().isEmpty()) {
                bootLogger.trace(() -> traceMessage(bm, null));
            }

            if (bm.isDeprecated()) {
                bootLogger.stderr(deprecationMessage(bm));
            }
        }

        for (BuiltModule bm : builtModules) {
            for (Class<? extends BQModule> o : bm.getOverrides()) {
                BuiltModule overrideModule = moduleByClass.get(o);
                moduleGraph.add(bm, overrideModule);
                bootLogger.trace(() -> traceMessage(bm, overrideModule));
            }
        }

        List<BQModule> modules = new ArrayList<>(moduleByClass.size());
        moduleGraph.topSort().forEach(moduleClass -> modules.add(moduleClass.getModule()));
        return modules;
    }

    private String deprecationMessage(BuiltModule module) {
        return new StringBuilder("** Module '")
                .append(module.getModuleName())
                .append("' is deprecated")
                .append(module.getDescription() != null ? ". Module details: " : ".")
                .append(module.getDescription() != null ? module.getDescription() : "")
                .toString();
    }

    private String traceMessage(BuiltModule module, BuiltModule overrides) {
        StringBuilder message = new StringBuilder("Loading module '")
                .append(module.getModuleName())
                .append("'");

        boolean isDeprecated = module.isDeprecated();
        if (isDeprecated) {
            message.append(" ** DEPRECATED, ");
        }

        message.append(" provided by '").append(module.getProviderName()).append("'");

        if (overrides != null) {
            message.append(", overrides '").append(overrides.getModuleName()).append("'");
        }

        return message.toString();
    }
}
