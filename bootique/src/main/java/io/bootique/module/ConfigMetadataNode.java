package io.bootique.module;

/**
 * @since 0.21
 */
public interface ConfigMetadataNode {

    <T> T accept(ConfigMetadataVisitor<T> visitor);
}
