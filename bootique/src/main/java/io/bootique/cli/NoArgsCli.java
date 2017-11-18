package io.bootique.cli;

import joptsimple.OptionSpec;

import java.util.Collections;
import java.util.List;

/**
 * A Cli instance over an empty argument list.
 *
 * @since 0.25
 */
public final class NoArgsCli implements Cli {

    private static final Cli INSTANCE = new NoArgsCli();

    public static Cli getInstance() {
        return INSTANCE;
    }

    @Override
    public String commandName() {
        return null;
    }

    @Override
    public boolean hasOption(String name) {
        return false;
    }

    @Override
    public List<OptionSpec<?>> detectedOptions() {
        return Collections.emptyList();
    }

    @Override
    public List<String> optionStrings(String name) {
        return Collections.emptyList();
    }

    @Override
    public List<String> standaloneArguments() {
        return Collections.emptyList();
    }
}
