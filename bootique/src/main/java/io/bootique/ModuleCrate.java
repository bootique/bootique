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
import io.bootique.names.ClassToName;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A container of a single {@link BQModule} that holds the module itself and its metadata, including name, description,
 * references to overridden modules, etc. It is a temporary representation of module during Bootique app startup. It
 * can be created using the builder API. See {@link ModuleCrate#of(BQModule)}.
 *
 * @since 3.0
 */
public class ModuleCrate {

    private final BQModule module;
    private final String moduleName;
    private final String description;
    private final String providerName;
    private final boolean deprecated;
    private final Collection<Class<? extends BQModule>> overrides;
    private final Map<String, Type> configs;

    public static Builder of(BQModule module) {
        return new Builder(module);
    }

    /**
     * A factory method that clones a ModuleCrate, allowing to override some of its properties and produce a new crate.
     */
    public static Builder of(ModuleCrate proto) {
        return ModuleCrate.of(proto.getModule())
                .moduleName(proto.getModuleName())
                .description(proto.getDescription())
                .providerName(proto.getProviderName())
                .deprecated(proto.isDeprecated())
                .overrides(proto.getOverrides())
                .configs(proto.configs);
    }

    protected ModuleCrate(
            BQModule module,
            String moduleName,
            String providerName,
            String description,
            boolean deprecated,
            Collection<Class<? extends BQModule>> overrides,
            Map<String, Type> configs) {

        this.module = Objects.requireNonNull(module);
        this.moduleName = Objects.requireNonNull(moduleName);
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

        ModuleCrate that = (ModuleCrate) o;
        return typesEqual(module.getClass(), that.module.getClass());
    }

    @Override
    public int hashCode() {
        return module.getClass().hashCode();
    }

    private static boolean typesEqual(Class<?> c1, Class<?> c2) {

        // lambdas are assumed non-equal by default as we can't compare their closure values
        if (c1.isSynthetic()) {
            return false;
        }

        // Class inherits "equals" from Object, the second check is redundant. Keeping it here in case something changes
        // in the JDK in the future
        return c1 == c2 || c1.equals(c2);
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

        public ModuleCrate build() {
            return new ModuleCrate(
                    module,
                    moduleName != null ? moduleName : MODULE_NAME_BUILDER.toName(module.getClass()),
                    providerName != null
                            ? providerName
                            // "<self>" is a guess. We have no idea who provided the module unless explicitly told
                            : module instanceof BQModuleProvider ? "<self>" : "<unknown>",
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
