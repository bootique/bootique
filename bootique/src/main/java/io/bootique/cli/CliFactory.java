package io.bootique.cli;

import io.bootique.command.Command;

public interface CliFactory {

    Cli createCli(String[] args);

    Cli createCli(Command command, String[] args);
}
