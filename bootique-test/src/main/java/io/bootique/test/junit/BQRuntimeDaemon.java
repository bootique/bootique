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
 *
 * @deprecated since 3.0.M1, as we are we phasing out JUnit 4 support in favor of JUnit 5
 */
@Deprecated
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
     */
    public Optional<CommandOutcome> getOutcome() {
        return Optional.ofNullable(outcome);
    }

    public BQRuntime getRuntime() {
        return runtime;
    }

    public void start() {
        this.executor.submit(() -> {
            try {
                outcome = runtime.run();
            } catch (Exception ex) {
                outcome = CommandOutcome.failed(-1, ex);
            }
        });
        checkStartupSucceeded(startupTimeout, startupTimeoutTimeUnit);
    }

    private long startupCheckSleepInterval(long remainingTimeoutMs, int round) {

        final long increment = 40;

        if (remainingTimeoutMs <= increment) {
            return increment;
        }

        // start with low wait time, and increase it a bit with every round, never to exceed the timeout
        long sleepFor = increment + round * increment;
        return sleepFor < remainingTimeoutMs  ? sleepFor : remainingTimeoutMs - increment;
    }

    private Optional<CommandOutcome> checkStartupOutcome() {
        // Either the command has finished, or it is still running, but the custom check is successful.
        // The later test may be used for blocking commands that start some background processing and
        // wait till the end.

        if (outcome != null) {
            return Optional.of(outcome);
        }

        if (startupCheck.apply(runtime)) {
            // command is still running (perhaps waiting for a background task execution, or listening for
            // requests), but the stack is in the state that can be tested already.
            return Optional.of(CommandOutcome.succeededAndForkedToBackground());
        }

        logger.stderr("Daemon runtime hasn't started yet...");
        return Optional.empty();
    }

    protected void checkStartupSucceeded(long timeout, TimeUnit unit) {

        long startedWaitingMs = System.currentTimeMillis();
        long timeoutMs = unit.toMillis(timeout);

        Future<CommandOutcome> startupFuture = executor.submit(() -> {

            try {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {

                    Optional<CommandOutcome> optOutcome = checkStartupOutcome();
                    if (optOutcome.isPresent()) {
                        return optOutcome.get();
                    }

                    long remainingTimeout = timeoutMs - (System.currentTimeMillis() - startedWaitingMs);
                    Thread.sleep(startupCheckSleepInterval(remainingTimeout, i));
                }

                // should never end up here with any reasonable timeouts
                return CommandOutcome.failed(-1, "Exceeded the number of attempts to check for successful startup");

            } catch (InterruptedException e) {
                logger.stderr("Interrupted waiting for server to start.. one last check..");
                return checkStartupOutcome().orElse(CommandOutcome.failed(-1, e));
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
