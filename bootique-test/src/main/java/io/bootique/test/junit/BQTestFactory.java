package io.bootique.test.junit;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.config.ConfigurationFactory;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages a simple Bootique stack within a lifecycle of the a JUnit test. It doesn't run any commands by default and
 * is usually used for accessing initialized standard services, such as {@link ConfigurationFactory}, etc. Instances
 * should be annotated within the unit tests with {@link Rule} or {@link ClassRule}. E.g.:
 * <pre>
 * public class MyTest {
 *
 *   &#64;Rule
 *   public BQTestFactory testFactory = new BQTestFactory();
 * }
 * </pre>
 *
 * @since 0.15
 */
public class BQTestFactory extends ExternalResource {

    private Collection<BQRuntime> runtimes;
    private boolean autoLoadModules;

    @Override
    protected void after() {
        Collection<BQRuntime> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.forEach(runtime -> {
                try {
                    runtime.shutdown();
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
     * Sets the default policy for this factory to auto-load modules for each app.
     *
     * @return this factory instance.
     * @since 0.25
     */
    public BQTestFactory autoLoadModules() {
        this.autoLoadModules = true;
        return this;
    }

    /**
     * @param args a String vararg emulating shell arguments passed to a real app.
     * @return a new instance of builder for the test runtime stack.
     * @since 0.20
     */
    public Builder app(String... args) {
        Builder builder = new Builder(runtimes, args);

        if (autoLoadModules) {
            builder.autoLoadModules();
        }

        return builder;
    }

    public static class Builder extends BQTestRuntimeBuilder<Builder> {

        private Collection<BQRuntime> runtimes;

        private Builder(Collection<BQRuntime> runtimes, String[] args) {
            super(args);
            this.runtimes = runtimes;
        }

        /**
         * The main build method that creates and returns a {@link BQRuntime}.
         *
         * @return a new instance of {@link BQRuntime} configured in this builder.
         */
        public BQRuntime createRuntime() {
            BQRuntime runtime = bootique.createRuntime();
            runtimes.add(runtime);
            return runtime;
        }

        /**
         * A convenience shortcut for the tests that are interested in command outcome, not in the runtime state. This
         * code is equivalent to <code>createRuntime().run()</code>.
         *
         * @return an outcome of the application command.
         * @since 0.25
         */
        public CommandOutcome run() {
            return createRuntime().run();
        }
    }
}
