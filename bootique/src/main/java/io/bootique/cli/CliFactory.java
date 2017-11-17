package io.bootique.cli;

/**
 * @since 0.25
 */
public interface CliFactory {

    /**
     * Creates {@link Cli} instance from a list of command line arguments. Invoked command name will be deduced from the
     * arguments.
     *
     * @param args Command line arguments
     * @return a new Cli instance
     */
    Cli createCli(String[] args);

    /**
     * Creates Cli with an explicit command and  alist of arguments.
     *
     * @param command command to run
     * @param args    command  arguments
     * @return Cli instance
     * @since 0.25
     */
    Cli createCli(String command, String[] args);
}
