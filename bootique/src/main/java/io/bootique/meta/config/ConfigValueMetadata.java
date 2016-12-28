package io.bootique.meta.config;

import io.bootique.meta.MetadataNode;

/**
 * Descriptor of a configuration value property.
 *
 * @since 0.21
 */
public class ConfigValueMetadata extends MetadataNode implements ConfigMetadataNode {

    protected Class<?> type;

    protected ConfigValueMetadata() {
    }

    public static Builder builder() {
        return new Builder(new ConfigValueMetadata());
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitConfigPropertyMetadata(this);
    }

    public Class<?> getType() {
        return type;
    }

    // parameterization is needed to enable covariant return types in subclasses
    public static class Builder<T extends ConfigValueMetadata, B extends Builder<T, B>> {

        protected T toBuild;

        protected Builder(T toBuild) {
            this.toBuild = toBuild;
        }

        public T build() {
            return toBuild;
        }

        public B name(String name) {
            toBuild.name = name;
            return (B) this;
        }

        public B description(String description) {
            if (description != null && description.length() == 0) {
                description = null;
            }

            toBuild.description = description;
            return (B) this;
        }

        public B type(Class<?> type) {
            toBuild.type = type;
            return (B) this;
        }
    }
}
