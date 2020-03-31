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
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A JUnit 5 extension that allows to create one or more Bootique runtimes within a JUnit test, performing automatic
 * shutdown of all created runtimes after the end of each test method. You would normally add this extension to the
 * test programmatically, either as static or an instance variables annotated with
 * {@link org.junit.jupiter.api.extension.RegisterExtension}. E.g.:
 *
 * <pre>
 * public class MyTest {
 *
 *   &#64;RegisterExtension
 *   public static BQTestFactory testFactory = new BQTestFactory();
 * }
 * </pre>
 *
 * @since 2.0
 */
public class BQTestFactory implements BeforeEachCallback, AfterEachCallback {

    private TestRuntimesManager runtimes;
    private boolean autoLoadModules;

    public BQTestFactory() {
        this.runtimes = new TestRuntimesManager();
    }

    /**
     * Sets the default policy for this factory to auto-load modules for each app.
     */
    public BQTestFactory autoLoadModules() {
        this.autoLoadModules = true;
        return this;
    }

    /**
     * @param args a String vararg emulating shell arguments passed to a real app.
     * @return a new instance of builder for the test runtime stack.
     */
    public Builder app(String... args) {
        Builder builder = new Builder(runtimes, args);

        if (autoLoadModules) {
            builder.autoLoadModules();
        }

        return builder;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        runtimes.reset();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        runtimes.shutdown();
    }

    public static class Builder {

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

        private Builder(TestRuntimesManager runtimes, String[] args) {
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
        public Builder args(String... args) {
            bootique.args(args);
            return this;
        }

        /**
         * Appends extra values to the test CLI arguments.
         *
         * @param args extra args to pass to Bootique.
         * @return this instance of test runtime builder.
         */
        public Builder args(Collection<String> args) {
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
        public Builder autoLoadModules() {
            bootique.autoLoadModules();
            return this;
        }

        /**
         * @param bootLogger custom BootLogger to use for a given runtime.
         * @return this instance of test runtime builder.
         */
        public Builder bootLogger(BootLogger bootLogger) {
            bootique.bootLogger(bootLogger);
            return this;
        }

        /**
         * @param moduleType custom Module class to add to Bootique DI runtime.
         * @return this instance of test runtime builder.
         * @see #autoLoadModules()
         */
        public Builder module(Class<? extends BQModule> moduleType) {
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
        public final Builder modules(Class<? extends BQModule>... moduleTypes) {
            bootique.modules(moduleTypes);
            return this;
        }

        /**
         * @param m a module to add to the test runtime.
         * @return this instance of test runtime builder.
         */
        public Builder module(BQModule m) {
            bootique.module(m);
            return this;
        }

        /**
         * Adds an array of Modules to the Bootique DI runtime.
         *
         * @param modules an array of modules to add to Bootiqie DI runtime.
         * @return this instance of test runtime builder.
         */
        public Builder modules(BQModule... modules) {
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
        public Builder module(BQModuleProvider moduleProvider) {
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
        public BQModuleOverrideBuilder<Builder> override(Class<? extends BQModule>... overriddenTypes) {

            BQModuleOverrideBuilder<Bootique> subBuilder = bootique.override(overriddenTypes);
            return new BQModuleOverrideBuilder<Builder>() {

                @Override
                public Builder with(Class<? extends BQModule> moduleType) {
                    subBuilder.with(moduleType);
                    return Builder.this;
                }

                @Override
                public Builder with(BQModule module) {
                    subBuilder.with(module);
                    return Builder.this;
                }
            };
        }

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }
    }
}
