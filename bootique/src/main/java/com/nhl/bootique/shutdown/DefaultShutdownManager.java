package com.nhl.bootique.shutdown;

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
