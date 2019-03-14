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

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class RuntimeModuleMerger {

    private BootLogger bootLogger;

    RuntimeModuleMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    Collection<Module> toGuiceModules(Collection<BQModule> bqModules) {
        return applyOverrides(checkCycles(collectUnique(bqModules)));
    }

    private Collection<Module> applyOverrides(Collection<RuntimeModule> modules) {
        return modules.stream()
                // find "heads" in override dependency linked lists.
                .filter(RuntimeModule::doesNotOverrideOthers)
                // fold each overrides linked list into a single module
                .map(this::fold)
                .collect(toList());
    }

    private Collection<RuntimeModule> checkCycles(Collection<RuntimeModule> modules) {
        modules.forEach(RuntimeModule::checkCycles);
        return modules;
    }

    private Collection<RuntimeModule> collectUnique(Collection<BQModule> bqModules) {

        // TODO: looking up modules by java type limits the use of lambdas as modules. E.g. we loaded test
        //  properties are dynamically created modules in a repeatedly called Lambda. This didn't work..
        //  So perhaps use provider name as a unique key?

        Map<Class<? extends Module>, RuntimeModule> map = new LinkedHashMap<>();

        for (BQModule bqModule : bqModules) {

            // TODO: we are not checking whether BQModule's overrides are present. Absent overrides will be skipped from
            //  the tree analysys and creation of override modules. We've never seen a problem with that because standard
            //  Bootique modules are auto-loaded (so if the module class is on classpath, there will be a BQModule).
            //  But auto-loading is not a given...

            RuntimeModule rm = new RuntimeModule(bqModule);

            RuntimeModule existing = map.putIfAbsent(rm.getModuleType(), rm);
            if (existing != null) {
                bootLogger.trace(() -> String.format(
                        "Skipping module '%s' provided by '%s' (already provided by '%s')...",
                        rm.getModuleName(),
                        rm.getProviderName(),
                        existing.getProviderName()));
            }
        }

        calcOverrideGraph(map);

        return map.values();
    }

    private void calcOverrideGraph(Map<Class<? extends Module>, RuntimeModule> modules) {

        for (RuntimeModule rm : modules.values()) {

            for(Class<? extends Module> override : rm.getBqModule().getOverrides()) {
                RuntimeModule rmn = modules.get(override);
                if(rmn != null) {
                    rmn.setOverriddenBy(rm);
                    rm.setOverridesOthers(true);
                }
            }
        }
    }

    private Module fold(RuntimeModule rm) {

        RuntimeModule overriddenBy = rm.getOverriddenBy();

        if (overriddenBy == null) {
            trace(rm.getBqModule(), null);
            return rm.getModule();
        }

        trace(rm.getBqModule(), overriddenBy.getBqModule());

        // WARN: using recursion because fold.. is there a realistic prospect of this blowing the stack? I haven't
        // seen overrides more than 2-4 levels deep.

        // fold must happen in this order (overriding starts from the tail). Otherwise the algorithm will not work.
        return Modules.override(rm.getModule()).with(fold(overriddenBy));
    }


    private void trace(BQModule module, BQModule overriddenBy) {
        bootLogger.trace(() -> traceMessage(module, overriddenBy));
    }

    private String traceMessage(BQModule module, BQModule overriddenBy) {

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
