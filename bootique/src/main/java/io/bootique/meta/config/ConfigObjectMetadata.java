package io.bootique.meta.config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Descriptor of a configuration object.
 *
 * @since 0.21
 */
public class ConfigObjectMetadata extends ConfigValueMetadata implements ConfigMetadataNode {

    private Collection<ConfigValueMetadata> properties;

    public ConfigObjectMetadata() {
        this.properties = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder(new ConfigObjectMetadata());
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitConfigMetadata(this);
    }

    public Collection<ConfigValueMetadata> getProperties() {
        return properties;
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigObjectMetadata, Builder> {

        public Builder(ConfigObjectMetadata toBuild) {
            super(toBuild);
        }

        public Builder addProperty(ConfigValueMetadata property) {
            toBuild.properties.add(property);
            return this;
        }

        public Builder addProperties(Collection<? extends ConfigValueMetadata> properties) {
            toBuild.properties.addAll(properties);
            return this;
        }
    }
}
