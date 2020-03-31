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
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.command.CommandOutcome;
import io.bootique.di.BQModule;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0
 */
public class TestRuntumeBuilder {

    /**
     * Properties are used to make test stack independent from a shell environment.
     * It allows us be sure that all the vars are controlled within the test and there are no outside influences.
     * <p>
     * The properties take part on {@link io.bootique.env.Environment} provision in {@link io.bootique.BQCoreModule}.
     */
    private static final String EXCLUDE_SYSTEM_VARIABLES = "bq.core.excludeSystemVariables";
    private static final String EXCLUDE_SYSTEM_PROPERTIES = "bq.core.excludeSystemProperties";

    private Bootique bootique;
    private Map<String, String> properties;
    private TestRuntimesManager runtimes;

    TestRuntumeBuilder(TestRuntimesManager runtimes, String[] args) {
        // exclude system variables and properties by setting the properties
        Map<String, String> properties = new HashMap<>();
        properties.put(EXCLUDE_SYSTEM_PROPERTIES, "true");
        properties.put(EXCLUDE_SYSTEM_VARIABLES, "true");

        this.properties = properties;
        this.bootique = Bootique.app(args).module(createPropertiesProvider());
        this.runtimes = runtimes;
    }

    /**
     * The main build method that creates and returns a {@link BQRuntime}.
     *
     * @return a new instance of {@link BQRuntime} configured in this builder.
     */
    public BQRuntime createRuntime() {
        BQRuntime runtime = bootique.createRuntime();
        runtimes.add(runtime);
        return runtime;
    }

    /**
     * A convenience shortcut for the tests that are interested in command outcome, not in the runtime state. This
     * code is equivalent to <code>createRuntime().run()</code>.
     *
     * @return an outcome of the application command.
     */
    public CommandOutcome run() {
        return createRuntime().run();
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
    public TestRuntumeBuilder args(String... args) {
        bootique.args(args);
        return this;
    }

    /**
     * Appends extra values to the test CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of test runtime builder.
     */
    public TestRuntumeBuilder args(Collection<String> args) {
        bootique.args(args);
        return this;
    }

    /**
     * Instructs Bootique to load any modules available on classpath that expose {@link BQModuleProvider}
     * provider. Auto-loaded modules will be used in default configuration. Factories within modules will of course be
     * configured dynamically from YAML.
     *
     * @return this instance of test runtime builder.
     */
    public TestRuntumeBuilder autoLoadModules() {
        bootique.autoLoadModules();
        return this;
    }

    /**
     * @param bootLogger custom BootLogger to use for a given runtime.
     * @return this instance of test runtime builder.
     */
    public TestRuntumeBuilder bootLogger(BootLogger bootLogger) {
        bootique.bootLogger(bootLogger);
        return this;
    }

    /**
     * @param moduleType custom Module class to add to Bootique DI runtime.
     * @return this instance of test runtime builder.
     * @see #autoLoadModules()
     */
    public TestRuntumeBuilder module(Class<? extends BQModule> moduleType) {
        bootique.module(moduleType);
        return this;
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
    public final TestRuntumeBuilder modules(Class<? extends BQModule>... moduleTypes) {
        bootique.modules(moduleTypes);
        return this;
    }

    /**
     * @param m a module to add to the test runtime.
     * @return this instance of test runtime builder.
     */
    public TestRuntumeBuilder module(BQModule m) {
        bootique.module(m);
        return this;
    }

    /**
     * Adds an array of Modules to the Bootique DI runtime.
     *
     * @param modules an array of modules to add to Bootiqie DI runtime.
     * @return this instance of test runtime builder.
     */
    public TestRuntumeBuilder modules(BQModule... modules) {
        bootique.modules(modules);
        return this;
    }

    /**
     * Adds a Module generated by the provider. Provider may optionally specify
     * that the Module overrides services in some other Module.
     *
     * @param moduleProvider a provider of Module and override spec.
     * @return this instance of test runtime builder.
     */
    public TestRuntumeBuilder module(BQModuleProvider moduleProvider) {
        bootique.module(moduleProvider);
        return this;
    }

    /**
     * Starts an API call chain to override an array of Modules.
     *
     * @param overriddenTypes an array of modules whose bindings should be overridden.
     * @return {@link BQModuleOverrideBuilder} object to specify a Module
     * overriding other modules.
     */
    public BQModuleOverrideBuilder<TestRuntumeBuilder> override(Class<? extends BQModule>... overriddenTypes) {

        BQModuleOverrideBuilder<Bootique> subBuilder = bootique.override(overriddenTypes);
        return new BQModuleOverrideBuilder<TestRuntumeBuilder>() {

            @Override
            public TestRuntumeBuilder with(Class<? extends BQModule> moduleType) {
                subBuilder.with(moduleType);
                return TestRuntumeBuilder.this;
            }

            @Override
            public TestRuntumeBuilder with(BQModule module) {
                subBuilder.with(module);
                return TestRuntumeBuilder.this;
            }
        };
    }

    public TestRuntumeBuilder property(String key, String value) {
        properties.put(key, value);
        return this;
    }
}
