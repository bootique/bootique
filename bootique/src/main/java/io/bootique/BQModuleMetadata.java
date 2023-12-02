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

import io.bootique.names.ClassToName;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A thin wrapper around Bootique DI module that helps Bootique to extract module metadata and override dependencies.
 *
 * @since 2.0
 * @deprecated replaced by {@link ModuleCrate}
 */
@Deprecated(since = "3.0", forRemoval = true)
public class BQModuleMetadata {

    // for now module names are simple class names... maybe change this to use Maven module names?
    protected static final ClassToName MODULE_NAME_BUILDER = ClassToName.builder().build();

    private final BQModule module;
    private final BQModuleId moduleId;
    private final String name;
    private final String description;
    private final String providerName;
    private final Collection<Class<? extends BQModule>> overrides;
    private final Map<String, Type> configs;

    private BQModuleMetadata(
            BQModule module,
            BQModuleId moduleId,
            String name,
            String description,
            String providerName,
            Collection<Class<? extends BQModule>> overrides,
            Map<String, Type> configs) {

        this.module = Objects.requireNonNull(module);
        this.moduleId = Objects.requireNonNull(moduleId);
        this.name = name;
        this.description = description;
        this.providerName = providerName;
        this.overrides = overrides;
        this.configs = configs;
    }

    public static Builder builder(BQModule module) {
        return new Builder(module);
    }

    /**
     * @since 3.0
     */
    public ModuleCrate toCrate() {
        return ModuleCrate.of(module)
                .moduleName(name)
                .description(description)
                .providerName(providerName)
                .overrides(overrides)
                .configs(configs)
                .build();
    }

    public BQModule getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProviderName() {
        return providerName;
    }

    public Collection<Class<? extends BQModule>> getOverrides() {
        return overrides;
    }

    public Map<String, Type> getConfigs() {
        return configs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BQModuleMetadata that = (BQModuleMetadata) o;

        return moduleId.equals(that.moduleId);
    }

    @Override
    public int hashCode() {
        return moduleId.hashCode();
    }

    @Deprecated(since = "3.0", forRemoval = true)
    public static class Builder {

        private final BQModule module;
        private String name;
        private String description;
        private String providerName;
        private Collection<Class<? extends BQModule>> overrides;
        private Map<String, Type> configs;

        private Builder(BQModule module) {
            this.module = Objects.requireNonNull(module);
        }

        public BQModuleMetadata build() {

            return new BQModuleMetadata(
                    module,
                    BQModuleId.of(module),
                    name != null ? name : MODULE_NAME_BUILDER.toName(module.getClass()),
                    description,
                    providerName,
                    overrides,
                    configs
            );
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String descrption) {
            this.description = descrption;
            return this;
        }

        public Builder providerName(String name) {
            this.providerName = name;
            return this;
        }

        public Builder overrides(Collection<Class<? extends BQModule>> overrides) {
            this.overrides = overrides;
            return this;
        }

        public Builder configs(Map<String, Type> configs) {
            this.configs = configs;
            return this;
        }
    }
}
