package io.bootique.test.junit;

import io.bootique.BQRuntime;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.test.BQDaemonTestRuntime;
import io.bootique.test.InMemoryPrintStream;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Manages a "daemon" Bootique stack within a lifecycle of the a JUnit test.
 * This allows to start background servers so that tests can execute requests
 * against them, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * <p>
 * <pre>
 * public class MyTest {
 *
 * 	&#64;Rule
 * 	public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();
 * }
 * </pre>
 *
 * @since 0.15
 */
public class BQDaemonTestFactory extends ExternalResource {

    protected Collection<BQDaemonTestRuntime> runtimes;

    @Override
    protected void after() {
        Collection<BQDaemonTestRuntime> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.forEach(runtime -> {
                try {
                    runtime.stop();
                } catch (Exception e) {
                    // ignore...
                }
            });
        }
    }

    @Override
    protected void before() {
        this.runtimes = new ArrayList<>();
    }

    /**
     * @return a new instance of builder for the test runtime stack.
     * @deprecated since 0.20 in favor of {@link #app(String...)}.
     */
    public Builder newRuntime() {
        return app();
    }

    /**
     * @return a new instance of builder for the test runtime stack.
     * @since 0.20
     */
    public Builder app(String... args) {
        return new Builder(runtimes, args);
    }

    public static class Builder<T extends Builder<T>> extends BQTestRuntimeBuilder<T> {

        private static final Function<BQDaemonTestRuntime, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

        private Collection<BQDaemonTestRuntime> runtimes;
        private Function<BQDaemonTestRuntime, Boolean> startupCheck;
        private long startupTimeout;
        private TimeUnit startupTimeoutTimeUnit;

        protected Builder(Collection<BQDaemonTestRuntime> runtimes, String[] args) {
            super(args);
            this.startupTimeout = 5;
            this.startupTimeoutTimeUnit = TimeUnit.SECONDS;
            this.runtimes = runtimes;
            this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
        }

        public T startupCheck(Function<BQDaemonTestRuntime, Boolean> startupCheck) {
            this.startupCheck = Objects.requireNonNull(startupCheck);
            return (T) this;
        }

        /**
         * Adds a startup check that waits till the runtime finishes, within the
         * startup timeout bounds.
         *
         * @return this builder
         * @since 0.16
         */
        public T startupAndWaitCheck() {
            this.startupCheck = (runtime) -> runtime.getOutcome().isPresent();
            return (T) this;
        }

        public T startupTimeout(long timeout, TimeUnit unit) {
            this.startupTimeout = timeout;
            this.startupTimeoutTimeUnit = unit;
            return (T) this;
        }

        /**
         * @return {@link BQDaemonTestRuntime} instance created by the builder.
         * @deprecated since 0.20 in favor of no-argument {@link #start()}. Arguments can be passed when creating the
         * Builder.
         */
        @Deprecated
        public BQDaemonTestRuntime start(String... args) {
            bootique.args(args);
            return start();
        }

        /**
         * Starts the test app in a background thread.
         *
         * @return {@link BQDaemonTestRuntime} instance created by the builder. The caller doesn't need to shut it down.
         * Usually JUnit lifecycle takes care of it.
         * @since 0.20
         */
        public BQDaemonTestRuntime start() {

            InMemoryPrintStream stdout = new InMemoryPrintStream(System.out);
            InMemoryPrintStream stderr = new InMemoryPrintStream(System.err);

            // TODO: allow to turn off tracing, which can be either useful or annoying dependning on the context...
            BootLogger bootLogger = new DefaultBootLogger(true, stdout, stderr);

            BQRuntime runtime = bootique.bootLogger(bootLogger).createRuntime();
            BQDaemonTestRuntime testRuntime = new BQDaemonTestRuntime(runtime, stdout, stderr, startupCheck);
            runtimes.add(testRuntime);

            testRuntime.start(startupTimeout, startupTimeoutTimeUnit);
            return testRuntime;
        }
    }
}
