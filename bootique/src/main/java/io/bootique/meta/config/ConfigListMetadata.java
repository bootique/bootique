package io.bootique.meta.config;

import java.util.List;
import java.util.Objects;

/**
 * @since 0.21
 */
public class ConfigListMetadata extends ConfigValueMetadata {

    private ConfigValueMetadata elementType;

    public static Builder builder() {
        return new Builder(new ConfigListMetadata()).type(List.class);
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitConfigListMetadata(this);
    }

    public ConfigValueMetadata getElementType() {
        return elementType;
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigListMetadata, ConfigListMetadata.Builder> {

        public Builder(ConfigListMetadata toBuild) {
            super(toBuild);
        }

        @Override
        public ConfigListMetadata build() {
            Objects.requireNonNull(toBuild.elementType);
            return super.build();
        }

        public Builder elementType(ConfigValueMetadata elementType) {
            toBuild.elementType = elementType;
            return this;
        }
    }
}
