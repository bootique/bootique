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

package io.bootique.meta.module;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Injectable object that holds metadata of each DI module present in the app runtime.
 */
public class ModulesMetadata {

    private final Collection<ModuleMetadata> modules;

    ModulesMetadata(Collection<ModuleMetadata> modules) {
        this.modules = modules;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ModuleMetadata module, ModuleMetadata... moreModules) {
        Builder b = builder().addModule(module);

        if (moreModules != null) {
            b.addModules(asList(moreModules));
        }

        return b;
    }

    /**
     * Returns a collection of metadata objects, one for each DI module.
     *
     * @return a collection of application DI modules.
     */
    public Collection<ModuleMetadata> getModules() {
        return modules;
    }

    public static class Builder {

        private final Collection<ModuleMetadata> modules;

        private Builder() {
            this.modules = new ArrayList<>();
        }

        public ModulesMetadata build() {
            return new ModulesMetadata(modules);
        }

        public Builder addModule(ModuleMetadata moduleMetadata) {
            modules.add(moduleMetadata);
            return this;
        }

        public Builder addModules(Collection<ModuleMetadata> moduleMetadata) {
            modules.addAll(moduleMetadata);
            return this;
        }
    }
}
