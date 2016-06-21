package com.nhl.bootique.test;

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;

import java.util.function.Consumer;

/**
 * A base class of test "shells" that allow to run various modules in Bootique
 * environment.
 *
 * @since 0.14
 */
public class BQTestRuntime {

    private InMemoryPrintStream stdout;
    private InMemoryPrintStream stderr;
    private Consumer<Bootique> configurator;

    private BQRuntime runtime;

    public BQTestRuntime(Consumer<Bootique> configurator, String... args) {
        this.stdout = new InMemoryPrintStream(System.out);
        this.stderr = new InMemoryPrintStream(System.err);
        this.configurator = configurator;
        this.runtime = createRuntime(args);
    }

    /**
     * @return internal BQRuntime.
     * @since 0.16
     */
    public BQRuntime getRuntime() {
        return runtime;
    }

    public String getStdout() {
        return stdout.toString();
    }

    public String getStderr() {
        return stderr.toString();
    }

    protected BootLogger createBootLogger() {
        return new DefaultBootLogger(true, stdout, stderr);
    }

    protected BQRuntime createRuntime(String... args) {
        Bootique bootique = Bootique.app(args).bootLogger(createBootLogger());
        configurator.accept(bootique);
        return bootique.createRuntime();
    }

    /**
     * Executes runtime runner, returning the outcome.
     */
    public CommandOutcome run() {
        CommandOutcome result = runtime.getRunner().run();
        if (!result.isSuccess()) {

            String message = result.getMessage() != null
                    ? "Error executing runtime: " + result.getMessage()
                    : "Error executing runtime";
            runtime.getBootLogger().stderr(message, result.getException());
        }

        return result;
    }

    public void stop() {
        runtime.shutdown();
    }
}
