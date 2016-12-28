package io.bootique.meta.config;

/**
 * @since 0.21
 */
public interface ConfigMetadataVisitor<T> {

    default T visitObjectMetadata(ConfigObjectMetadata metadata) {
        return null;
    }

    default T visitValueMetadata(ConfigValueMetadata metadata) {
        return null;
    }

    default T visitListMetadata(ConfigListMetadata metadata) {
        return null;
    }

    default T visitMapMetadata(ConfigMapMetadata metadata) {
        return null;
    }
}
