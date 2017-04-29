package io.bootique;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.bootique.annotation.Args;
import io.bootique.command.CommandOutcome;
import io.bootique.log.BootLogger;
import io.bootique.run.Runner;
import io.bootique.shutdown.ShutdownManager;

import java.util.Arrays;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * A wrapper around launcher DI container.
 */
public class BQRuntime {

    private Injector injector;

    public BQRuntime(Injector injector) {
        this.injector = injector;
    }

    /**
     * Returns a DI-bound instance of a given class, throwing if this class is not explicitly bound in DI.
     *
     * @param <T>  a type of the instance to return.
     * @param type a class or interface bound in DI.
     * @return a DI-bound instance of a given type.
     * @since 0.12
     */
    public <T> T getInstance(Class<T> type) {
        Binding<T> binding = injector.getExistingBinding(Key.get(type));

        // note that Guice default behavior is to attempt creating a binding on
        // the fly, if there's no explicit one available. We are overriding this
        // behavior.
        return Objects.requireNonNull(binding, "No binding for type: " + type).getProvider().get();
    }

    public BootLogger getBootLogger() {
        return getInstance(BootLogger.class);
    }

    /**
     * @return an instance of {@link Runner} DI singleton.
     * @deprecated since 0.23. Just use 'getInstance(Runner.class)' or {@link #run()} if you simply need to execute the
     * run sequence.
     */
    @Deprecated
    public Runner getRunner() {
        return getInstance(Runner.class);
    }

    /**
     * Locates internal {@link Runner} and calls its run method.
     *
     * @return outcome of the runner execution.
     * @since 0.23
     */
    public CommandOutcome run() {
        return getInstance(Runner.class).run();
    }

    public String[] getArgs() {
        return injector.getInstance(Key.get(String[].class, Args.class));
    }

    /**
     * @return a String representation of CLI arguments array
     * @deprecated since 0.23 this method is unused by Bootique.
     */
    @Deprecated
    public String getArgsAsString() {
        return Arrays.asList(getArgs()).stream().collect(joining(" "));
    }

    /**
     * Registers a JVM shutdown hook that is delegated to {@link ShutdownManager}.
     *
     * @see Bootique#exec()
     * @since 0.11
     * @deprecated since 0.23 unused as {@link Bootique} class handles JVM-level shutdown events. BQRuntime should not
     * get involved in that.
     */
    @Deprecated
    public void addJVMShutdownHook() {

        // resolve all Injector services needed for shutdown eagerly and outside
        // shutdown thread to ensure that shutdown hook will not fail due to
        // misconfiguration, etc.

        ShutdownManager shutdownManager = injector.getInstance(ShutdownManager.class);
        BootLogger logger = getBootLogger();

        Runtime.getRuntime().addShutdownHook(new Thread("bootique-shutdown") {

            @Override
            public void run() {
                shutdown(shutdownManager, logger);
            }
        });
    }

    /**
     * Executes Bootique runtime shutdown, allowing all interested DI services to perform cleanup.
     *
     * @since 0.12
     */
    public void shutdown() {
        ShutdownManager shutdownManager = injector.getInstance(ShutdownManager.class);
        BootLogger logger = getBootLogger();

        shutdown(shutdownManager, logger);
    }

    protected void shutdown(ShutdownManager shutdownManager, BootLogger logger) {
        shutdownManager.shutdown().forEach((s, th) -> {
            logger.stderr(String.format("Error performing shutdown of '%s': %s", s.getClass().getSimpleName(),
                    th.getMessage()));
        });
    }
}
