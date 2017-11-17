package io.bootique.cli;

/**
 * @since 0.25
 */
public interface CliFactory {

    /**
     * Creates a {@link Cli} instance from a list of command line arguments. Invoked command name will be deduced from the
     * arguments.
     *
     * @param args Command line arguments
     * @return a new Cli instance
     */
    Cli createCli(String[] args);

    /**
     * Creates a {@link Cli} instance with an explicit command and a list of extra arguments. Same as
     * {@link #createCli(String[])}, except used in cases when the command name is known in advance.
     *
     * @param command a name of the command to run.
     * @param args    command  arguments
     * @return Cli instance
     * @since 0.25
     */
    Cli createCli(String command, String[] args);
}
