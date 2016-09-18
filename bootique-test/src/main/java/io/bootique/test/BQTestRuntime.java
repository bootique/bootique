package io.bootique.test;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;

/**
 * A base class of test "shells" that allow to run various modules in Bootique
 * environment.
 *
 * @since 0.14
 */
public class BQTestRuntime {

    private InMemoryPrintStream stdout;
    private InMemoryPrintStream stderr;
    private BQRuntime runtime;

    public BQTestRuntime(BQRuntime runtime, InMemoryPrintStream stdout, InMemoryPrintStream stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.runtime = runtime;
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
