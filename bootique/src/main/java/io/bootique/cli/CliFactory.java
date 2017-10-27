package io.bootique.cli;

public interface CliFactory {

    Cli createCli(String[] args);
}
