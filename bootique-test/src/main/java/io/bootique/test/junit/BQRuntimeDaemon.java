package io.bootique.test.junit;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A wrapper around {@link BQRuntime} that runs it on the background and handles startup and shutdown sequence.
 */
public class BQRuntimeDaemon {

    private BootLogger logger;
    private BQRuntime runtime;
    private long startupTimeout;
    private TimeUnit startupTimeoutTimeUnit;
    private ExecutorService executor;
    private Function<BQRuntime, Boolean> startupCheck;
    private Optional<CommandOutcome> outcome;

    public BQRuntimeDaemon(BQRuntime runtime, Function<BQRuntime, Boolean> startupCheck, long startupTimeout, TimeUnit startupTimeoutTimeUnit) {

        // use a separate logger from the tested process to avoid mixing STDERR output
        this.logger = new DefaultBootLogger(false);

        this.runtime = runtime;
        this.startupCheck = startupCheck;
        this.executor = Executors.newCachedThreadPool();
        this.outcome = Optional.empty();
        this.startupTimeout = startupTimeout;
        this.startupTimeoutTimeUnit = startupTimeoutTimeUnit;
    }

    /**
     * @return an optional outcome, available if the test runtime has finished.
     * @since 0.16
     */
    public Optional<CommandOutcome> getOutcome() {
        return outcome;
    }

    public BQRuntime getRuntime() {
        return runtime;
    }

    public void start() {
        this.executor.submit(() -> outcome = Optional.of(runtime.run()));
        checkStartupSucceeded(startupTimeout, startupTimeoutTimeUnit);
    }

    protected void checkStartupSucceeded(long timeout, TimeUnit unit) {

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
            logger.stderr("Daemon runtime started...");
        } else {
            throw new RuntimeException("Daemon failed to start");
        }
    }

    public void stop() {

        runtime.shutdown();

        // must interrupt execution (using "shutdown()" is not enough to stop
        // Jetty for instance
        executor.shutdownNow();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
            logger.stderr("Daemon runtime stopped...");
        } catch (InterruptedException e) {
            logger.stderr("Interrupted while waiting for shutdown", e);
        }
    }
}
