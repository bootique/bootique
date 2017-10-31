package io.bootique.cli;

import io.bootique.command.Command;

/**
 * @since 0.25
 */
public interface CliFactory {

    /**
     * Create Cli from a list of command line arguments.
     * Default command name will be deduced from the list of arguments.
     *
     * @param args Command line arguments
     * @return Cli instance
     * @since 0.25
     */
    Cli createCli(String[] args);

    /**
     * Create Cli with an explicit default command from a list of command line arguments.
     *
     * @param defaultCommand Default command type
     * @param args Command line arguments
     * @return Cli instance
     * @since 0.25
     */
    Cli createCli(Command defaultCommand, String[] args);
}
