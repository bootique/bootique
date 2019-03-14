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

import java.util.*;
import java.util.stream.Collectors;

class RuntimeModule {

    private BootLogger bootLogger;
    private BQModule bqModule;
    private RuntimeModule overriddenBy;
    private Collection<RuntimeModule> overridden;

    RuntimeModule(BQModule bqModule, BootLogger bootLogger) {
        this.bqModule = bqModule;
        this.bootLogger = bootLogger;
    }

    public Module resolve() {

        bootLogger.trace(() -> traceMessage(bqModule, null));

        if (overridden == null) {
            return bqModule.getModule();
        }

        return resolve(new OverrideLevel()).toGuiceModule();
    }

    private OverrideLevel resolve(OverrideLevel level) {

        level.addModule(bqModule.getModule());

        // WARN: using recursion... is there a realistic prospect of this blowing the stack? I haven't seen overrides
        // more than 2-4 levels deep.

        if (overridden != null) {
            OverrideLevel subLevel = level.getOrCreateSubLevel();

            for (RuntimeModule rm : overridden) {
                bootLogger.trace(() -> traceMessage(rm.getBqModule(), bqModule));
                rm.resolve(subLevel);
            }
        }

        return level;
    }

    public BQModule getBqModule() {
        return bqModule;
    }

    void checkCycles() {
        if (overriddenBy != null) {
            overriddenBy.checkCycles(this, new ArrayList<>());
        }
    }

    private void checkCycles(RuntimeModule root, List<RuntimeModule> trace) {
        trace.add(this);

        if (root == this) {
            // Add next level, to make error message more clear.
            trace.add(this.overriddenBy);
            throw new BootiqueException(1,
                    "Circular override dependency between DI modules: " +
                            trace.stream().map(rm -> rm.bqModule.getName()).collect(Collectors.joining(" -> ")));
        }

        if (overriddenBy != null) {
            overriddenBy.checkCycles(root, trace);
        }
    }

    Class<? extends Module> getModuleType() {
        return bqModule.getModule().getClass();
    }

    String getModuleName() {
        return bqModule.getName();
    }

    String getProviderName() {
        return bqModule.getProviderName();
    }

    boolean isTop() {
        return overriddenBy == null;
    }

    void addOverridden(RuntimeModule overridden) {

        overridden.setOverriddenBy(this);

        if (this.overridden == null) {
            this.overridden = new ArrayList<>(4);
        }

        this.overridden.add(overridden);
    }

    private void setOverriddenBy(RuntimeModule module) {

        // no more than one override is allowed
        if (this.overriddenBy != null) {
            String message = String.format(
                    "Module %s provided by %s is overridden twice by %s and %s",
                    getModuleName(),
                    getProviderName(),
                    this.overriddenBy.getModuleName(),
                    module.getModuleName());

            throw new BootiqueException(1, message);
        }

        this.overriddenBy = module;
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

    // stores overridden modules at a given tree depth regardless of overriding module
    static class OverrideLevel {

        List<Module> modules;
        OverrideLevel subLevel;

        public OverrideLevel getOrCreateSubLevel() {

            if (this.subLevel == null) {
                this.subLevel = new OverrideLevel();
            }

            return subLevel;
        }

        void addModule(Module module) {
            if (this.modules == null) {
                this.modules = new ArrayList<>();
            }

            this.modules.add(module);
        }

        /**
         * Converts top level to Guice module with overrides.
         */
        Module toGuiceModule() {
            if (modules == null || modules.isEmpty()) {
                throw new IllegalStateException("No parent module");
            }

            Module m = modules.get(0);
            return subLevel != null ? subLevel.toGuiceModule(m) : m;
        }

        private Module toGuiceModule(Module parent) {

            if (modules == null) {
                return parent;
            }

            // Guice actually allows multiple parents for multiple children.. Can we take advantage of that in some form?
            Module overridden = Modules.override(modules).with(parent);
            return subLevel != null ? subLevel.toGuiceModule(overridden) : overridden;
        }
    }
}
