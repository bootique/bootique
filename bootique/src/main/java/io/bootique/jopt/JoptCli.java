package io.bootique.jopt;

import io.bootique.cli.Cli;
import joptsimple.OptionSet;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * {@link Cli} implementation on top of jopt-simple library.
 */
public class JoptCli implements Cli {

    private OptionSet optionSet;
    private String commandName;

    public JoptCli(OptionSet optionSet, String commandName) {
        this.optionSet = optionSet;
        this.commandName = commandName;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public boolean hasOption(String optionName) {
        return optionSet.has(optionName);
    }

    @Override
    public List<String> optionStrings(String name) {
        return optionSet.valuesOf(name).stream().map(o -> String.valueOf(o)).collect(toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> standaloneArguments() {
        return (List<String>) optionSet.nonOptionArguments();
    }
}
