package io.bootique.jopt;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.annotation.Args;
import io.bootique.cli.Cli;
import io.bootique.cli.CliFactory;

public class JoptCliProvider implements Provider<Cli> {

    private CliFactory cliFactory;
    private String[] args;

    @Inject
    public JoptCliProvider(CliFactory cliFactory, @Args String[]args) {
        this.cliFactory = cliFactory;
        this.args = args;
    }

    @Override
    public Cli get() {
        return cliFactory.createCli(args);
    }

}
