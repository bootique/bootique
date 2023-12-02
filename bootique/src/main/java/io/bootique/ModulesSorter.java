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

import io.bootique.log.BootLogger;

import java.util.*;

class ModulesSorter {

    private final BootLogger bootLogger;

    ModulesSorter(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    /**
     * Returns a list of unique crates in the correct load order with respect to overrides.
     */
    List<ModuleCrate> uniqueCratesInLoadOrder(Collection<ModuleCrate> crates) {

        int inLen = crates.size();
        ModuleGraph moduleGraph = new ModuleGraph(inLen);
        Map<Class<? extends BQModule>, ModuleCrate> moduleByClass = new HashMap<>((int) Math.ceil(inLen / 0.9f), 0.9f);

        for (ModuleCrate bm : crates) {
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

        for (ModuleCrate bm : crates) {
            for (Class<? extends BQModule> o : bm.getOverrides()) {
                ModuleCrate overrideModule = moduleByClass.get(o);
                moduleGraph.add(bm, overrideModule);
                bootLogger.trace(() -> traceMessage(bm, overrideModule));
            }
        }

        return moduleGraph.topSort();
    }

    private String deprecationMessage(ModuleCrate module) {
        return new StringBuilder("** Deprecation alert - ")
                .append(module.getModuleName())
                .append(module.getDescription() != null ? ": " : ".")
                .append(module.getDescription() != null ? module.getDescription() : "")
                .toString();
    }

    private String traceMessage(ModuleCrate module, ModuleCrate overrides) {
        StringBuilder message = new StringBuilder("Loading module '")
                .append(module.getModuleName())
                .append("'");

        boolean isDeprecated = module.isDeprecated();
        if (isDeprecated) {
            message.append(" ** DEPRECATED, ");
        }

        if (overrides != null) {
            message.append(" overrides '").append(overrides.getModuleName()).append("'");
        }

        return message.toString();
    }
}
