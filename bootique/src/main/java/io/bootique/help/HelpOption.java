package io.bootique.help;

import io.bootique.cli.meta.CliOption;

import java.util.Objects;

/**
 * A wrapper for {@link io.bootique.cli.meta.CliOption} that handles option sorting, short names and conflicts.
 *
 * @since 0.20
 */
public class HelpOption implements Comparable<HelpOption> {

    private CliOption option;
    private boolean shortNameAllowed;

    public HelpOption(CliOption option) {
        this.option = Objects.requireNonNull(option);
        this.shortNameAllowed = true;
    }

    @Override
    public int compareTo(HelpOption o) {
        return option.getName().compareTo(o.getOption().getName());
    }

    public CliOption getOption() {
        return option;
    }

    public String getShortName() {
        return option.getName().substring(0, 1);
    }

    public boolean isShortNameAllowed() {
        return shortNameAllowed;
    }

    public void setShortNameAllowed(boolean shortNameAllowed) {
        this.shortNameAllowed = shortNameAllowed;
    }
}
