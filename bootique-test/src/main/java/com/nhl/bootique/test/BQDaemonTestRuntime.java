package com.nhl.bootique.test;

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

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;

/**
 * @since 0.13
 */
public class BQDaemonTestRuntime extends BQTestRuntime {

	private ExecutorService executor;
	private Function<BQDaemonTestRuntime, Boolean> startupCheck;
	private Optional<CommandOutcome> outcome;

	private BQRuntime runtime;

	public BQDaemonTestRuntime(Consumer<Bootique> configurator, Function<BQDaemonTestRuntime, Boolean> startupCheck) {
		super(configurator);

		this.startupCheck = startupCheck;
		this.executor = Executors.newCachedThreadPool();
		this.outcome = Optional.empty();
	}

	/**
	 * @since 0.16
	 * @return an optional outcome, available if the test runtime has finished.
	 */
	public Optional<CommandOutcome> getOutcome() {
		return outcome;
	}

	/**
	 * @since 0.16
	 * @return internal BQRuntime.
	 */
	public BQRuntime getRuntime() {
		return runtime;
	}

	public void start(long timeout, TimeUnit unit, String... args) {

		start(args);

		BootLogger logger = runtime.getBootLogger();

		Future<Boolean> startupFuture = executor.submit(() -> {

			try {
				while (!startupCheck.apply(this)) {
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
			logger.stdout("Daemon runtime started successfully...");
		} else {
			throw new RuntimeException("Daemon failed to start");
		}
	}

	protected void start(String... args) {
		this.runtime = createRuntime(args);
		this.executor.submit(() -> run());
	}

	public void stop() {

		if (runtime == null) {
			// this means we weren't started successfully. No need to shutdown
			return;
		}

		BootLogger logger = runtime.getBootLogger();

		// must interrupt execution (using "shutdown()" is not enough to stop
		// Jetty for instance
		executor.shutdownNow();
		try {
			executor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.stderr("Interrupted while waiting for shutdown", e);
		}
	}

	protected void run() {
		Objects.requireNonNull(runtime);
		try {
			this.outcome = Optional.of(runtime.getRunner().run());
		} finally {
			runtime.shutdown();
		}
	}
}
