package io.bootique.unit;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.log.BootLogger;

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
	public Builder newRuntime() {
		return new Builder(runtimes, executor);
	}

	public static class Builder extends BQInternalTestFactory.Builder {

		private static final Function<BQRuntime, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

		private ExecutorService executor;
		private Function<BQRuntime, Boolean> startupCheck;
		private long startupTimeout;
		private TimeUnit startupTimeoutTimeUnit;

		protected Builder(Collection<BQRuntime> runtimes, ExecutorService executor) {

			super(runtimes);

			this.executor = executor;
			this.startupTimeout = 5;
			this.startupTimeoutTimeUnit = TimeUnit.SECONDS;
			this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
		}

		@Override
		public Builder property(String key, String value) {
			return (Builder) super.property(key, value);
		}

		@Override
		public Builder configurator(Consumer<Bootique> configurator) {
			return (Builder) super.configurator(configurator);
		}

		@Override
		public Builder var(String key, String value) {
			return (Builder) super.var(key, value);
		}

		public Builder startupCheck(Function<BQRuntime, Boolean> startupCheck) {
			this.startupCheck = Objects.requireNonNull(startupCheck);
			return this;
		}

		public Builder startupTimeout(long timeout, TimeUnit unit) {
			this.startupTimeout = timeout;
			this.startupTimeoutTimeUnit = unit;
			return this;
		}

		@Override
		public BQRuntime build(String... args) {

			BQRuntime runtime = super.build(args);

			LOGGER.info("Starting runtime...");
			executor.submit(() -> Optional.of(runtime.getRunner().run()));
			checkStartupSucceeded(runtime, startupTimeout, startupTimeoutTimeUnit);
			return runtime;
		}

		protected void checkStartupSucceeded(BQRuntime runtime, long timeout, TimeUnit unit) {
			BootLogger logger = runtime.getBootLogger();

			Future<Boolean> startupFuture = executor.submit(() -> {

				try {
					while (!startupCheck.apply(runtime)) {
						logger.stderr("Daemon runtime hasn't started yet...");
						Thread.sleep(500);
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
