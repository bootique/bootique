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

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.config.ConfigurationFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages a simple Bootique stack within a lifecycle of the a JUnit test. It doesn't run any commands by default and
 * is usually used for accessing initialized standard services, such as {@link ConfigurationFactory}, etc. Instances
 * should be annotated within the unit tests with {@link org.junit.jupiter.api.extension.RegisterExtension}. E.g.:
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

    private Collection<BQRuntime> runtimes;
    private boolean autoLoadModules;

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
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        Collection<BQRuntime> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.forEach(runtime -> {
                try {
                    runtime.shutdown();
                } catch (Exception e) {
                    // ignore...
                }
            });
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        this.runtimes = new ArrayList<>();
    }

    public static class Builder extends BQTestRuntimeBuilder<Builder> {

        private Collection<BQRuntime> runtimes;

        private Builder(Collection<BQRuntime> runtimes, String[] args) {
            super(args);
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
    }
}
