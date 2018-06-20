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

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A shutdown handler that performs a best-effort attempt to shutdown a set of
 * {@link AutoCloseable} objects, blocking no longer then the specified timeout.
 * 
 * @since 0.11
 */
public class DefaultShutdownManager implements ShutdownManager {

	private Duration timeout;
	private ConcurrentMap<AutoCloseable, Integer> shutdownHooks;

	public DefaultShutdownManager(Duration timeout) {
		this.shutdownHooks = new ConcurrentHashMap<>();
		this.timeout = timeout;
	}

	@Override
	public void addShutdownHook(AutoCloseable shutdownHook) {
		shutdownHooks.put(shutdownHook, 1);
	}

	@Override
	public Map<?, ? extends Throwable> shutdown() {

		Map<?, ? extends Throwable> shutdownErrors;

		// TODO: closing services often involves slow I/O; perhaps this needs to
		// be done in multiple threads?

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Map<?, ? extends Throwable>> future = executor.submit(() -> shutdownAll());

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
		shutdownHooks.keySet().forEach(c -> shutdownOne(c).ifPresent(e -> errors.put(c, e)));
		return errors;
	}

	protected Optional<Exception> shutdownOne(AutoCloseable closeable) {
		try {
			closeable.close();
			return Optional.empty();
		} catch (Exception e) {
			return Optional.of(e);
		}
	}
}
