/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique;

import com.google.inject.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RuntimeModule {

    private BQModule bqModule;
    private RuntimeModule overriddenBy;
    private boolean overridesOthers;

    RuntimeModule(BQModule bqModule) {
        this.bqModule = bqModule;
    }

    public Module getModule() {
        return bqModule.getModule();
    }

    public BQModule getBqModule() {
        return bqModule;
    }

    public RuntimeModule getOverriddenBy() {
        return overriddenBy;
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
        return getModule().getClass();
    }

    String getModuleName() {
        return bqModule.getName();
    }

    String getProviderName() {
        return bqModule.getProviderName();
    }

    boolean doesNotOverrideOthers() {
        return !overridesOthers;
    }

    public void setOverridesOthers(boolean overridesOthers) {
        this.overridesOthers = overridesOthers;
    }

    void setOverriddenBy(RuntimeModule module) {

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
}
