package io.bootique.meta.config;

/**
 * @since 0.21
 */
public interface ConfigMetadataNode {

    <T> T accept(ConfigMetadataVisitor<T> visitor);
}
