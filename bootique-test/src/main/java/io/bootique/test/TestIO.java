package io.bootique.test;

import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;

/**
 * Encapsulates tested process STDIN and STDERR streams.
 *
 * @since 0.23
 */
public class TestIO {

    private InMemoryPrintStream stdout;
    private InMemoryPrintStream stderr;
    private boolean trace;

    public static TestIO noTrace() {
        return create(false);
    }

    public static TestIO trace() {
        return create(true);
    }

    private static TestIO create(boolean trace) {
        return new TestIO(new InMemoryPrintStream(System.out), new InMemoryPrintStream(System.err), trace);
    }

    protected TestIO(InMemoryPrintStream stdout, InMemoryPrintStream stderr, boolean trace) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.trace = trace;
    }

    public BootLogger getBootLogger() {
        return new DefaultBootLogger(trace, stdout, stderr);
    }

    public String getStderr() {
        return stderr.toString();
    }

    public String getStdout() {
        return stdout.toString();
    }
}
