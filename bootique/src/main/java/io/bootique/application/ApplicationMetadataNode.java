package io.bootique.application;

/**
 * A base superclass of the app metadata nodes.
 *
 * @since 0.20
 */
public abstract class ApplicationMetadataNode {

    protected String name;
    protected String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
