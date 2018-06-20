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

package io.bootique.unit;

import io.bootique.BQRuntime;
import io.bootique.log.BootLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class BQInternalDaemonTestFactory extends BQInternalTestFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BQInternalDaemonTestFactory.class);

    protected ExecutorService executor;

    @Override
    protected void before() {
        this.executor = Executors.newCachedThreadPool();
        super.before();
    }

    @Override
    protected void after() {
        super.after();

        executor.shutdownNow();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Builder app(String... args) {
        return new Builder(runtimes, executor, args);
    }

    public static class Builder<T extends Builder<T>> extends BQInternalTestFactory.Builder<T> {

        private static final Function<BQRuntime, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

        private ExecutorService executor;
        private Function<BQRuntime, Boolean> startupCheck;
        private long startupTimeout;
        private TimeUnit startupTimeoutTimeUnit;

        protected Builder(Collection<BQRuntime> runtimes, ExecutorService executor, String[] args) {

            super(runtimes, args);

            this.executor = executor;
            this.startupTimeout = 5;
            this.startupTimeoutTimeUnit = TimeUnit.SECONDS;
            this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
        }

        public T startupCheck(Function<BQRuntime, Boolean> startupCheck) {
            this.startupCheck = Objects.requireNonNull(startupCheck);
            return (T) this;
        }

        public T startupTimeout(long timeout, TimeUnit unit) {
            this.startupTimeout = timeout;
            this.startupTimeoutTimeUnit = unit;
            return (T) this;
        }

        @Override
        public BQRuntime createRuntime() {
            BQRuntime runtime = super.createRuntime();
            LOGGER.info("Starting runtime...");
            executor.submit(() -> Optional.of(runtime.run()));
            checkStartupSucceeded(runtime, startupTimeout, startupTimeoutTimeUnit);
            return runtime;
        }

        protected void checkStartupSucceeded(BQRuntime runtime, long timeout, TimeUnit unit) {
            BootLogger logger = runtime.getBootLogger();

            Future<Boolean> startupFuture = executor.submit(() -> {

                try {
                    while (!startupCheck.apply(runtime)) {
                        logger.stderr("Daemon runtime hasn't started yet...");
                        Thread.sleep(100);
                    }

                    return true;
                } catch (InterruptedException e) {
                    logger.stderr("Timed out waiting for server to start");
                    return false;
                } catch (Throwable th) {
                    logger.stderr("Server error", th);
                    return false;
                }

            });

            boolean success;
            try {
                success = startupFuture.get(timeout, unit);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(String.format("Daemon failed to start in %s ms", unit.toMillis(timeout)));
            }

            if (success) {
                LOGGER.info("Runtime started...");
            } else {
                throw new RuntimeException("Daemon failed to start");
            }
        }
    }
}
