package io.bootique.meta;

/**
 * A base superclass of metadata tree nodes.
 *
 * @since 0.21
 */
public abstract class MetadataNode {

    protected String name;
    protected String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
