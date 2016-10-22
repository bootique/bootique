package io.bootique.module;

import io.bootique.application.ApplicationMetadataNode;

/**
 * Descriptor of a configuration value property.
 *
 * @since 0.21
 */
public class ConfigPropertyMetadata extends ApplicationMetadataNode {

    protected Class<?> type;

    protected ConfigPropertyMetadata() {
    }

    public static Builder builder() {
        return new Builder(new ConfigPropertyMetadata());
    }

    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitConfigPropertyMetadata(this);
    }

    public Class<?> getType() {
        return type;
    }

    // parameterization is needed to enable covariant return types in subclasses
    public static class Builder<T extends ConfigPropertyMetadata, B extends Builder<T, B>> {

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
            toBuild.description = description;
            return (B) this;
        }

        public B type(Class<?> type) {
            toBuild.type = type;
            return (B) this;
        }
    }
}
