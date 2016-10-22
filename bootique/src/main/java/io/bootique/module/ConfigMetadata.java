package io.bootique.module;

import java.util.Collection;

/**
 * Metadata about configuration object.
 *
 * @since 0.21
 */
public class ConfigMetadata extends ConfigMetadataProperty {

    private Collection<? extends ConfigMetadataProperty> properties;

    public Collection<? extends ConfigMetadataProperty> getProperties() {
        return properties;
    }
}
