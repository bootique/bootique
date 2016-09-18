package io.bootique.test.junit;

import io.bootique.BQRuntime;
import io.bootique.config.ConfigurationFactory;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.test.BQTestRuntime;
import io.bootique.test.InMemoryPrintStream;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages a simple Bootique stack within a lifecycle of the a JUnit test. It
 * doesn't run any commands by default and is usually used for accessing
 * initialized standard services, such as {@link ConfigurationFactory}, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * <p>
 * <pre>
 * public class MyTest {
 *
 * 	&#64;Rule
 * 	public BQTestFactory testFactory = new BQTestFactory();
 * }
 * </pre>
 *
 * @since 0.15
 */
public class BQTestFactory extends ExternalResource {

    private Collection<BQTestRuntime> runtimes;

    @Override
    protected void after() {
        Collection<BQTestRuntime> localRuntimes = this.runtimes;

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

    public static class Builder extends BQTestRuntimeBuilder<Builder> {

        private Collection<BQTestRuntime> runtimes;

        private Builder(Collection<BQTestRuntime> runtimes, String[] args) {
            super(args);
            this.runtimes = runtimes;
        }

        /**
         * @param args arguments for the test stack app.
         * @return a new instance of test runtime.
         * @deprecated since 0.20 in favor of {@link #createRuntime()}.
         */
        @Deprecated
        public BQTestRuntime build(String... args) {
            bootique.args(args);
            return createRuntime();
        }

        /**
         * The main build method that creates and returns a {@link BQTestRuntime}, which is a thin wrapper for
         * Bootique runtime.
         *
         * @return a new instance of {@link BQTestRuntime} configured in this builder.
         */
        public BQTestRuntime createRuntime() {

            InMemoryPrintStream stdout = new InMemoryPrintStream(System.out);
            InMemoryPrintStream stderr = new InMemoryPrintStream(System.err);

            // TODO: allow to turn off tracing, which can be either useful or annoying dependning on the context...
            BootLogger bootLogger = new DefaultBootLogger(true, stdout, stderr);

            BQRuntime runtime = bootique.bootLogger(bootLogger).createRuntime();
            BQTestRuntime testRuntime = new BQTestRuntime(runtime, stdout, stderr);
            runtimes.add(testRuntime);
            return testRuntime;
        }
    }
}
