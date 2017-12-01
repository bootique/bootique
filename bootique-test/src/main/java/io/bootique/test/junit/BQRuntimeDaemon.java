package io.bootique.test.junit;

import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
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
    private CommandOutcome outcome;

    public BQRuntimeDaemon(BQRuntime runtime, Function<BQRuntime, Boolean> startupCheck, long startupTimeout, TimeUnit startupTimeoutTimeUnit) {

        // use a separate logger from the tested process to avoid mixing STDERR output
        this.logger = new DefaultBootLogger(false);

        this.runtime = runtime;
        this.startupCheck = startupCheck;
        this.executor = Executors.newCachedThreadPool();
        this.startupTimeout = startupTimeout;
        this.startupTimeoutTimeUnit = startupTimeoutTimeUnit;
    }

    /**
     * @return an optional outcome, available if the test runtime has finished.
     * @since 0.16
     */
    public Optional<CommandOutcome> getOutcome() {
        return Optional.ofNullable(outcome);
    }

    public BQRuntime getRuntime() {
        return runtime;
    }

    public void start() {
        this.executor.submit(() -> outcome = runtime.run());
        checkStartupSucceeded(startupTimeout, startupTimeoutTimeUnit);
    }

    protected void checkStartupSucceeded(long timeout, TimeUnit unit) {

        Future<CommandOutcome> startupFuture = executor.submit(() -> {

            try {
                // Either the command has finished, or it is still running, but the custom check is successful.
                // The later test may be used for blocking commands that start some background processing and
                // wait till the end.
                while (true) {

                    if(outcome != null) {
                        return outcome;
                    }

                    if(startupCheck.apply(runtime)) {
                        // command is still running (perhaps waiting for a background task execution, or listening for
                        // requests), but the stack is in the state that can be tested already.
                        return CommandOutcome.succeededAndForkedToBackground();
                    }

                    logger.stderr("Daemon runtime hasn't started yet...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                logger.stderr("Timed out waiting for server to start");
                return CommandOutcome.failed(-1, e);
            } catch (Throwable th) {
                logger.stderr("Server error", th);
                return CommandOutcome.failed(-1, th);
            }
        });

        CommandOutcome outcome;
        try {
            outcome = startupFuture.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(String.format("Daemon failed to start in %s ms", unit.toMillis(timeout)));
        }

        if (outcome.isSuccess()) {
            logger.stderr("Daemon runtime started...");
        } else {
            throw new BootiqueException(outcome.getExitCode(), "Daemon failed to start: " + outcome);
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
