package io.bootique.meta.config;

/**
 * @since 0.21
 */
public interface ConfigMetadataVisitor<T> {

    default T visitConfigMetadata(ConfigObjectMetadata metadata) {
        return null;
    }

    default T visitConfigPropertyMetadata(ConfigValueMetadata metadata) {
        return null;
    }

    default T visitConfigListMetadata(ConfigListMetadata metadata) {
        return null;
    }

    default T visitConfigMapMetadata(ConfigMapMetadata metadata) {
        return null;
    }
}
