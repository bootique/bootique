package io.bootique.meta.config;

import java.util.Map;
import java.util.Objects;

/**
 * @since 0.21
 */
public class ConfigMapMetadata extends ConfigValueMetadata {

    private Class<?> keysType;
    private ConfigValueMetadata valuesType;

    public static Builder builder() {
        return new Builder(new ConfigMapMetadata()).type(Map.class);
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitConfigMapMetadata(this);
    }

    public Class<?> getKeysType() {
        return keysType;
    }

    public ConfigValueMetadata getValuesType() {
        return valuesType;
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigMapMetadata, ConfigMapMetadata.Builder> {

        public Builder(ConfigMapMetadata toBuild) {
            super(toBuild);
        }

        @Override
        public ConfigMapMetadata build() {
            Objects.requireNonNull(toBuild.valuesType);
            return super.build();
        }

        public Builder keysType(Class<?> keysType) {
            toBuild.keysType = keysType;
            return this;
        }

        public Builder valuesType(ConfigValueMetadata elementType) {
            toBuild.valuesType = elementType;
            return this;
        }
    }
}
