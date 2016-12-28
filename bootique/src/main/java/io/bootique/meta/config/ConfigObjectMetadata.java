package io.bootique.meta.config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Descriptor of a configuration object.
 *
 * @since 0.21
 */
public class ConfigObjectMetadata extends ConfigValueMetadata {

    private Collection<ConfigMetadataNode> properties;

    public ConfigObjectMetadata() {
        this.properties = new ArrayList<>();
    }

    /**
     * Returns a builder that starts with an already available object. Occasionally it is useful for the caller to
     * provide the initial object, e.g. to cache the instance before the builder is finished to avoid compilation cycles.
     *
     * @param baseObject an initial object that will be further customized by the builder.
     * @return a builder that starts with the provided base object instead of creating a new one.
     */
    public static Builder builder(ConfigObjectMetadata baseObject) {
        return new Builder(baseObject);
    }

    public static Builder builder() {
        return builder(new ConfigObjectMetadata());
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitObjectMetadata(this);
    }

    public Collection<ConfigMetadataNode> getProperties() {
        return properties;
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigObjectMetadata, Builder> {

        public Builder(ConfigObjectMetadata toBuild) {
            super(toBuild);
        }

        public Builder addProperty(ConfigMetadataNode property) {
            toBuild.properties.add(property);
            return this;
        }

        public Builder addProperties(Collection<? extends ConfigValueMetadata> properties) {
            toBuild.properties.addAll(properties);
            return this;
        }
    }
}
