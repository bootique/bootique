package io.bootique.cli;

/**
 * @since 0.25
 */
public interface CliFactory {

    /**
     * Create {@link Cli} instance from a list of command line arguments. Invoked command name will be deduced from the
     * list of arguments.
     *
     * @param args Command line arguments
     * @return a new Cli instance
     */
    Cli createCli(String[] args);
}
