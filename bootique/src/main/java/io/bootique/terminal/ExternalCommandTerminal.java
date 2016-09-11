package io.bootique.terminal;

import io.bootique.log.BootLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A superclass of {@link Terminal} implementations that need to run shell commands to query the terminal state.
 *
 * @since 0.20
 */
public abstract class ExternalCommandTerminal implements Terminal {

    protected BootLogger logger;

    public ExternalCommandTerminal(BootLogger logger) {
        this.logger = logger;
    }

    protected static String toString(String[] array) {
        return String.join(" ", array);
    }

    protected abstract String[] getColumnsCommand();

    protected abstract Integer parseColumns(BufferedReader commandOutput);

    @Override
    public int getColumns() {
        String[] command = getColumnsCommand();
        Integer columns = runCommand(command, this::parseColumns);
        return columns != null ? columns.intValue() : 0;
    }

    protected <T> T runCommand(String[] command, Function<BufferedReader, T> resultParser) {
        try {
            return runCommandWithExceptions(command, resultParser);
        } catch (IOException | InterruptedException e) {
            logger.trace(() -> "Command '" + toString(command) + "' error: " + e.getMessage());
            return null;
        }
    }

    protected <T> T runCommandWithExceptions(String[] command, Function<BufferedReader, T> resultParser) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(command)
                // important to grab input from the Java process... THis provides a way for the terminal commands
                // like "stty" to make sense of the calling environment...
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .start();

        if (!p.waitFor(3, TimeUnit.SECONDS)) {
            logger.trace(() -> "Command '" + toString(command) + "' is stuck, killing");
            p.destroyForcibly();
            return null;
        }

        if (p.exitValue() != 0) {
            logger.trace(() -> "Command '" + toString(command) + "' failure. Exit code: " + p.exitValue());
            return null;
        }

        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            return resultParser.apply(r);
        }
    }
}
