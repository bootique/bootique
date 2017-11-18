package io.bootique.cli;

/**
 * @since 0.25
 */
public interface CliFactory {

    /**
     * Creates a {@link Cli} instance from a list of command line arguments.
     *
     * @param args Command line arguments
     * @return a new Cli instance
     */
    Cli createCli(String[] args);
}
