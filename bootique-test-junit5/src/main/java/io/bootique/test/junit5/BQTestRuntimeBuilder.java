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

package io.bootique.test.junit5;

import io.bootique.BQCoreModule;
import io.bootique.BQModuleOverrideBuilder;
import io.bootique.BQModuleProvider;
import io.bootique.Bootique;
import io.bootique.di.BQModule;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0
 */
// parameterization is needed to enable covariant return types in subclasses
public abstract class BQTestRuntimeBuilder<T extends BQTestRuntimeBuilder<T>> {

    protected Bootique bootique;
    protected Map<String, String> properties;

    /**
     * Properties are used to make test stack independent from a shell environment.
     * It allows us be sure that all the vars are controlled within the test and there are no outside influences.
     * <p>
     * The properties take part on {@link io.bootique.env.Environment} provision in {@link BQCoreModule}.
     */
    private static final String EXCLUDE_SYSTEM_VARIABLES = "bq.core.excludeSystemVariables";
    private static final String EXCLUDE_SYSTEM_PROPERTIES = "bq.core.excludeSystemProperties";

    protected BQTestRuntimeBuilder(String[] args) {
        //exclude system variables and properties by setting the properties
        final HashMap<String, String> properties = new HashMap<>();
        properties.put(EXCLUDE_SYSTEM_PROPERTIES, "true");
        properties.put(EXCLUDE_SYSTEM_VARIABLES, "true");

        this.properties = properties;
        this.bootique = Bootique.app(args).module(createPropertiesProvider());
    }

    protected BQModuleProvider createPropertiesProvider() {
        return new BQModuleProvider() {

            @Override
            public BQModule module() {
                return binder -> BQCoreModule.extend(binder).setProperties(properties);
            }

            @Override
            public String name() {
                return "BQTestRuntimeBuilder:properties";
            }
        };
    }

    /**
     * Appends extra values to the test CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of test runtime builder.
     */
    public T args(String... args) {
        bootique.args(args);
        return (T) this;
    }

    /**
     * Appends extra values to the test CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of test runtime builder.
     */
    public T args(Collection<String> args) {
        bootique.args(args);
        return (T) this;
    }

    /**
     * Instructs Bootique to load any modules available on classpath that expose {@link BQModuleProvider}
     * provider. Auto-loaded modules will be used in default configuration. Factories within modules will of course be
     * configured dynamically from YAML.
     *
     * @return this instance of test runtime builder.
     */
    public T autoLoadModules() {
        bootique.autoLoadModules();
        return (T) this;
    }

    /**
     * @param bootLogger custom BootLogger to use for a given runtime.
     * @return this instance of test runtime builder.
     * @since 0.23
     */
    public T bootLogger(BootLogger bootLogger) {
        bootique.bootLogger(bootLogger);
        return (T) this;
    }

    /**
     * @param moduleType custom Module class to add to Bootique DI runtime.
     * @return this instance of test runtime builder.
     * @see #autoLoadModules()
     */
    public T module(Class<? extends BQModule> moduleType) {
        bootique.module(moduleType);
        return (T) this;
    }

    /**
     * Adds an array of Module types to the Bootique DI runtime. Each type will
     * be instantiated by Bootique and added to the DI container.
     *
     * @param moduleTypes custom Module classes to add to Bootique DI runtime.
     * @return this instance of test runtime builder.
     * @see #autoLoadModules()
     */
    @SafeVarargs
    public final T modules(Class<? extends BQModule>... moduleTypes) {
        bootique.modules(moduleTypes);
        return (T) this;
    }

    /**
     * @param m a module to add to the test runtime.
     * @return this instance of test runtime builder.
     */
    public T module(BQModule m) {
        bootique.module(m);
        return (T) this;
    }

    /**
     * Adds an array of Modules to the Bootique DI runtime.
     *
     * @param modules an array of modules to add to Bootiqie DI runtime.
     * @return this instance of test runtime builder.
     */
    public T modules(BQModule... modules) {
        bootique.modules(modules);
        return (T) this;
    }

    /**
     * Adds a Module generated by the provider. Provider may optionally specify
     * that the Module overrides services in some other Module.
     *
     * @param moduleProvider a provider of Module and override spec.
     * @return this instance of test runtime builder.
     */
    public T module(BQModuleProvider moduleProvider) {
        bootique.module(moduleProvider);
        return (T) this;
    }

    /**
     * Starts an API call chain to override an array of Modules.
     *
     * @param overriddenTypes an array of modules whose bindings should be overridden.
     * @return {@link BQModuleOverrideBuilder} object to specify a Module
     * overriding other modules.
     */
    public BQModuleOverrideBuilder<T> override(Class<? extends BQModule>... overriddenTypes) {

        BQModuleOverrideBuilder<Bootique> subBuilder = bootique.override(overriddenTypes);
        return new BQModuleOverrideBuilder<T>() {

            @Override
            public T with(Class<? extends BQModule> moduleType) {
                subBuilder.with(moduleType);
                return (T) BQTestRuntimeBuilder.this;
            }

            @Override
            public T with(BQModule module) {
                subBuilder.with(module);
                return (T) BQTestRuntimeBuilder.this;
            }
        };
    }

    public T property(String key, String value) {
        properties.put(key, value);
        return (T) this;
    }
}
