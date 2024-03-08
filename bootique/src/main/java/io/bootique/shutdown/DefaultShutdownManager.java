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

package io.bootique.shutdown;

import io.bootique.log.BootLogger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A shutdown handler that performs a best-effort attempt to shutdown a set of {@link AutoCloseable} objects,
 * blocking no longer then the specified timeout.
 */
public class DefaultShutdownManager implements ShutdownManager {

    private final Duration timeout;
    private final BootLogger logger;
    private final ConcurrentMap<ShutdownTask<?>, Integer> shutdownHooks;

    public DefaultShutdownManager(Duration timeout, BootLogger logger) {
        this.timeout = timeout;
        this.logger = logger;
        this.shutdownHooks = new ConcurrentHashMap<>();
    }

    @Override
    public <T> T onShutdown(T object, ShutdownCallback<T> shutdownCallback) {
        shutdownHooks.put(new ShutdownTask<>(object, shutdownCallback), 1);
        return object;
    }

    @Override
    public Map<?, ? extends Throwable> shutdown() {

        Map<?, ? extends Throwable> shutdownErrors;

        // TODO: closing services often involves slow I/O; perhaps this needs to
        // be done in multiple threads?

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Map<?, ? extends Throwable>> future = executor.submit(this::shutdownAll);

        try {
            shutdownErrors = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            shutdownErrors = Collections.singletonMap(this, e);
        }

        executor.shutdownNow();

        return shutdownErrors;
    }

    protected Map<?, ? extends Throwable> shutdownAll() {
        Map<Object, Throwable> errors = new HashMap<>();
        shutdownHooks.keySet().forEach(c -> c.shutdown(logger).ifPresent(e -> errors.put(c, e)));
        return errors;
    }

    static class ShutdownTask<T> {
        final T object;
        final ShutdownCallback<T> shutdownCallback;

        ShutdownTask(T object, ShutdownCallback<T> shutdownCallback) {
            this.object = Objects.requireNonNull(object);
            this.shutdownCallback = Objects.requireNonNull(shutdownCallback);
        }

        Optional<Exception> shutdown(BootLogger logger) {
            try {
                shutdownWithExceptions(logger);
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(e);
            }
        }

        private void shutdownWithExceptions(BootLogger logger) throws Exception {
            logger.trace(() -> "Stopping "
                    + (object.getClass().isSynthetic() ? object.getClass().getName() : object.getClass().getSimpleName())
                    + "...");

            shutdownCallback.shutdown(object);
        }
    }
}
