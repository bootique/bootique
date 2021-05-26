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

package io.bootique.test.junit;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Manages a "daemon" Bootique stack within a lifecycle of the a JUnit test. This allows to start background servers so
 * that tests can execute requests against them, etc. Instances should be annotated within the unit tests with
 * {@link Rule} or {@link ClassRule}. E.g.:
 * <pre>
 * public class MyTest {
 *
 * 	&#64;Rule
 * 	public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();
 * }
 * </pre>
 */
public class BQDaemonTestFactory extends ExternalResource {

    protected Map<BQRuntime, BQRuntimeDaemon> runtimes;
    protected boolean autoLoadModules;

    static BQRuntimeDaemon getDaemon(Map<BQRuntime, BQRuntimeDaemon> runtimes, BQRuntime runtime) {
        return Objects
                .requireNonNull(runtimes.get(runtime), "Runtime is not registered with the factory: " + runtime);
    }

    @Override
    protected void after() {
        Map<BQRuntime, BQRuntimeDaemon> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.values().forEach(runtime -> {
                try {
                    runtime.stop();
                } catch (Exception e) {
                    // ignore...
                }
            });
        }
    }

    /**
     * Sets the default policy for this factory to auto-load modules for each app.
     *
     * @return this factory instance.
     */
    public BQDaemonTestFactory autoLoadModules() {
        this.autoLoadModules = true;
        return this;
    }


    @Override
    protected void before() {
        this.runtimes = new HashMap<>();
    }

    /**
     * @param <T>  a covariant builder type.
     * @param args a String vararg emulating shell arguments passed to a real app.
     * @return a new instance of builder for the test runtime stack.
     */
    public <T extends Builder<T>> Builder<T> app(String... args) {
        Builder builder = new Builder(runtimes, args);

        if (autoLoadModules) {
            builder.autoLoadModules();
        }

        return builder;
    }

    /**
     * @param runtime a runtime executing in the background.
     * @return an optional object wrapping the state of the runtime execution. If present, then the runtime
     * execution has finished.
     */
    public Optional<CommandOutcome> getOutcome(BQRuntime runtime) {
        return getDaemon(runtime).getOutcome();
    }

    /**
     * Starts the specified runtime on the background. If startup check was specified when building the runtime with
     * {@link Builder#startupCheck(Function)} and similar, blocks the calling thread until startup check succeeds or
     * times out.
     *
     * @param runtime a runtime being tested. Must be the runtime produced and managed by this factory.
     */
    public void start(BQRuntime runtime) {
        getDaemon(runtime).start();
    }

    protected BQRuntimeDaemon getDaemon(BQRuntime runtime) {
        return BQDaemonTestFactory.getDaemon(this.runtimes, runtime);
    }

    /**
     * Shuts down the specified runtime running on the background as well as the thread pool supporting its execution.
     *
     * @param runtime a runtime being tested. Must be the runtime produced and managed by this factory.
     */
    public void stop(BQRuntime runtime) {
        getDaemon(runtime).stop();
    }


    // parameterization is needed to enable covariant return types in subclasses
    public static class Builder<T extends Builder<T>> extends BQTestRuntimeBuilder<T> {

        private static final Function<BQRuntime, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

        private Map<BQRuntime, BQRuntimeDaemon> runtimes;
        private Function<BQRuntime, Boolean> startupCheck;
        private long startupTimeout;
        private TimeUnit startupTimeoutTimeUnit;

        protected Builder(Map<BQRuntime, BQRuntimeDaemon> runtimes, String[] args) {
            super(args);
            this.startupTimeout = 5;
            this.startupTimeoutTimeUnit = TimeUnit.SECONDS;
            this.runtimes = runtimes;
            this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
        }

        public T startupCheck(Function<BQRuntime, Boolean> startupCheck) {
            this.startupCheck = Objects.requireNonNull(startupCheck);
            return (T) this;
        }

        /**
         * Adds a startup check that waits till the runtime finishes, within the startup timeout bounds.
         *
         * @return this builder
         */
        public T startupAndWaitCheck() {
            this.startupCheck = runtime -> BQDaemonTestFactory.getDaemon(runtimes, runtime).getOutcome().isPresent();
            return (T) this;
        }

        public T startupTimeout(long timeout, TimeUnit unit) {
            this.startupTimeout = timeout;
            this.startupTimeoutTimeUnit = unit;
            return (T) this;
        }

        /**
         * Creates runtime without starting it. Can be started via {@link BQDaemonTestFactory#start(BQRuntime)}.
         *
         * @return newly created managed runtime.
         */
        public BQRuntime createRuntime() {
            BQRuntime runtime = bootique.createRuntime();

            // wrap in BQRuntimeDaemon to handle thread pool shutdown and startup checks.
            BQRuntimeDaemon testRuntime =
                    new BQRuntimeDaemon(runtime, startupCheck, startupTimeout, startupTimeoutTimeUnit);
            runtimes.put(runtime, testRuntime);

            return runtime;
        }

        /**
         * Starts the test app in a background thread, blocking the test thread until the startup checker succeeds.
         *
         * @return {@link BQRuntime} instance. The caller doesn't need to shut it down. JUnit lifecycle takes care of it.
         */
        public BQRuntime start() {
            BQRuntime runtime = createRuntime();
            getDaemon(runtimes, runtime).start();
            return runtime;
        }
    }
}
