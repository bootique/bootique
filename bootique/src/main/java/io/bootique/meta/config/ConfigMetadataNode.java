package io.bootique.meta.config;

import io.bootique.meta.MetadataNode;

import java.lang.reflect.Type;

/**
 * @since 0.21
 */
public interface ConfigMetadataNode extends MetadataNode {

    <T> T accept(ConfigMetadataVisitor<T> visitor);

    Type getType();
}
