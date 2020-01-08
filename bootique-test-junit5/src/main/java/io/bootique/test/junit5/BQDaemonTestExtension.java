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
import io.bootique.test.junit.BQRuntimeDaemon;
import io.bootique.test.junit.BQTestRuntimeBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class BQDaemonTestExtension implements BeforeEachCallback, AfterEachCallback {

    protected Map<BQRuntime, BQRuntimeDaemon> runtimes;
    protected boolean autoLoadModules;

    static BQRuntimeDaemon getDaemon(Map<BQRuntime, BQRuntimeDaemon> runtimes, BQRuntime runtime) {
        return Objects
                .requireNonNull(runtimes.get(runtime), "Runtime is not registered with the factory: " + runtime);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
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

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        this.runtimes = new HashMap<>();
    }

    public BQDaemonTestExtension autoLoadModules() {
        this.autoLoadModules = true;
        return this;
    }

    public <T extends Builder<T>> Builder<T> app(String... args) {
        Builder builder = new Builder(runtimes, args);

        if (autoLoadModules) {
            builder.autoLoadModules();
        }

        return builder;
    }

    public Optional<CommandOutcome> getOutcome(BQRuntime runtime) {
        return getDaemon(runtime).getOutcome();
    }

    public void start(BQRuntime runtime) {
        getDaemon(runtime).start();
    }

    protected BQRuntimeDaemon getDaemon(BQRuntime runtime) {
        return getDaemon(this.runtimes, runtime);
    }

    public void stop(BQRuntime runtime) {
        getDaemon(runtime).stop();
    }

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
         */
        public T startupAndWaitCheck() {
            this.startupCheck = runtime -> getDaemon(runtimes, runtime).getOutcome().isPresent();
            return (T) this;
        }

        public T startupTimeout(long timeout, TimeUnit unit) {
            this.startupTimeout = timeout;
            this.startupTimeoutTimeUnit = unit;
            return (T) this;
        }

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
         */
        public BQRuntime start() {
            BQRuntime runtime = createRuntime();
            getDaemon(runtimes, runtime).start();
            return runtime;
        }
    }
}
