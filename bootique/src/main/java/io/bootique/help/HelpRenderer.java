package io.bootique.help;

import io.bootique.cli.Cli;

/**
 * Formats and outputs help information.
 *
 * @since 0.20
 */
public interface HelpRenderer {

    void printHelp(Cli cli, Appendable out);
}
