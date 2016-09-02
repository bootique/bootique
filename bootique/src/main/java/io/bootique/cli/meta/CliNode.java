package io.bootique.cli.meta;

/**
 * @since 0.20
 */
public abstract class CliNode {

    protected String name;
    protected String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
