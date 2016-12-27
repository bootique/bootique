package io.bootique.module;

/**
 * @since 0.21
 */
public interface ConfigMetadataVisitor<T> {

    default T visitConfigMetadata(ConfigObjectMetadata metadata) {
        return null;
    }

    default T visitConfigPropertyMetadata(ConfigPropertyMetadata metadata) {
        return null;
    }

    default T visitConfigListMetadata(ConfigListMetadata metadata) {
        return null;
    }
}
