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

package io.bootique.bootstrap;

import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;
import io.bootique.names.ClassToName;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A temporary module wrapper used during Bootique app bootstrap that allows Bootique runtime to figure out module
 * overrides and extract metadata. Created with the builder obtained via {@link BuiltModule#of(BQModule)}.
 *
 * @since 3.0
 */
public class BuiltModule {

    private final BQModule module;
    private final String moduleName;
    private final BuiltModuleId moduleId;
    private final String description;
    private final String providerName;
    private final boolean deprecated;
    private final Collection<Class<? extends BQModule>> overrides;
    private final Map<String, Type> configs;

    public static Builder of(BQModule module) {
        return new Builder(module);
    }

    protected BuiltModule(
            BQModule module,
            String moduleName,
            String providerName,
            String description,
            boolean deprecated,
            Collection<Class<? extends BQModule>> overrides,
            Map<String, Type> configs) {

        this.module = Objects.requireNonNull(module);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.moduleId = BuiltModuleId.of(module);
        this.providerName = Objects.requireNonNull(providerName);
        this.description = description;
        this.deprecated = deprecated;
        this.overrides = Objects.requireNonNull(overrides);
        this.configs = Objects.requireNonNull(configs);
    }

    public BQModule getModule() {
        return module;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDescription() {
        return description;
    }

    public String getProviderName() {
        return providerName;
    }

    public boolean isDeprecated() {
        return deprecated;
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

        BuiltModule that = (BuiltModule) o;

        return moduleId.equals(that.moduleId);
    }

    @Override
    public int hashCode() {
        return moduleId.hashCode();
    }

    public static class Builder {

        // for now module names are simple class names... maybe change this to use Maven module names?
        protected static final ClassToName MODULE_NAME_BUILDER = ClassToName.builder().build();

        protected String providerName;
        protected final BQModule module;
        protected String moduleName;
        protected String description;
        protected Boolean deprecated;
        protected Collection<Class<? extends BQModule>> overrides;
        protected Map<String, Type> configs;

        protected Builder(BQModule module) {
            this.module = Objects.requireNonNull(module);
        }

        public BuiltModule build() {
            return new BuiltModule(
                    module,
                    moduleName != null ? moduleName : MODULE_NAME_BUILDER.toName(module.getClass()),
                    providerName != null ? providerName : "<unknown_provider>",
                    description,
                    deprecated != null ? deprecated : module.getClass().isAnnotationPresent(Deprecated.class),
                    overrides != null ? overrides : Collections.emptyList(),
                    configs != null ? configs : Collections.emptyMap()
            );
        }

        public Builder moduleName(String name) {
            this.moduleName = name;
            return this;
        }

        public Builder description(String descrption) {
            this.description = descrption;
            return this;
        }

        public Builder provider(BQModuleProvider provider) {
            return providerName(provider.getClass().getSimpleName());
        }

        public Builder providerName(String name) {
            this.providerName = name;
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder overrides(Collection<Class<? extends BQModule>> overrides) {
            this.overrides = overrides;
            return this;
        }

        @SafeVarargs
        public final Builder overrides(Class<? extends BQModule>... overrides) {
            this.overrides = List.of(overrides);
            return this;
        }

        public Builder config(String path, Type configType) {
            if (configs == null) {
                configs = new HashMap<>();
            }
            this.configs.put(path, configType);
            return this;
        }

        public Builder configs(Map<String, Type> configs) {

            if (configs == null || configs.isEmpty()) {
                return this;
            }

            if (this.configs == null) {
                this.configs = new HashMap<>();
            }
            this.configs.putAll(configs);
            return this;
        }
    }
}
