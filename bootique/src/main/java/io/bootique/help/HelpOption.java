package io.bootique.help;

import io.bootique.application.OptionMetadata;

import java.util.Objects;

/**
 * A wrapper for {@link OptionMetadata} that handles option sorting, short names and conflicts.
 *
 * @since 0.20
 */
public class HelpOption implements Comparable<HelpOption> {

    private OptionMetadata option;
    private boolean shortNameAllowed;
    private boolean longNameAllowed;

    public HelpOption(OptionMetadata option) {
        this.option = Objects.requireNonNull(option);
        this.shortNameAllowed = true;
        this.longNameAllowed = option.getName().length() > 1;
    }

    @Override
    public int compareTo(HelpOption o) {
        return option.getName().compareTo(o.getOption().getName());
    }

    public OptionMetadata getOption() {
        return option;
    }

    public String getShortName() {
        return option.getName().substring(0, 1);
    }

    public boolean isShortNameAllowed() {
        return shortNameAllowed;
    }

    public boolean isLongNameAllowed() {
        return longNameAllowed;
    }

    public void setShortNameAllowed(boolean shortNameAllowed) {
        this.shortNameAllowed = shortNameAllowed;
    }
}
