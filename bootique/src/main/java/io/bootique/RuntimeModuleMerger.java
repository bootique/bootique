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
import io.bootique.log.BootLogger;

import java.util.*;

class RuntimeModuleMerger {

    private BootLogger bootLogger;

    RuntimeModuleMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    Collection<Module> toGuiceModules(Collection<BQModule> bqModules) {
        Collection<RuntimeModule> rtModules = collectUnique(bqModules);
        rtModules.forEach(RuntimeModule::checkCycles);
        return resolveGuiceModules(rtModules);
    }

    private Collection<Module> resolveGuiceModules(Collection<RuntimeModule> modules) {

        List<Module> resolved = new ArrayList<>();
        for (RuntimeModule m : modules) {
            if (m.isTop()) {
                resolved.add(m.resolveModule());
            }
        }

        return resolved;
    }

    private Collection<RuntimeModule> collectUnique(Collection<BQModule> bqModules) {

        // TODO: looking up modules by java type limits the use of lambdas as modules. E.g. we loaded test
        //  properties are dynamically created modules in a repeatedly called Lambda. This didn't work..
        //  So perhaps use provider name as a unique key?

        Map<Class<? extends Module>, RuntimeModule> map = new LinkedHashMap<>();

        for (BQModule bqModule : bqModules) {

            // TODO: we are not checking whether BQModule's overrides are present. Absent overrides will be skipped from
            //  the tree analysis and creation of override modules. We've never seen a problem with that because standard
            //  Bootique modules are auto-loaded (so if the module class is on classpath, there will be a BQModule).
            //  But auto-loading is not a given...

            RuntimeModule rm = new RuntimeModule(bqModule, bootLogger);

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

            for (Class<? extends Module> override : rm.getBqModule().getOverrides()) {
                RuntimeModule rmn = modules.get(override);
                if (rmn != null) {
                    rm.addOverridden(rmn);
                }
                // else:
                // TODO: complain that the overridden module is not known (may happen when overriding modules
                //   when auto-loading is not in effect
            }
        }
    }
}
