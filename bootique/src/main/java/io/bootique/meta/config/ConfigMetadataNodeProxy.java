package io.bootique.meta.config;

import java.lang.reflect.Type;

/**
 * @since 0.21
 */
class ConfigMetadataNodeProxy implements ConfigMetadataNode {

    private String name;
    private String description;
    private ConfigMetadataNode delegate;

    ConfigMetadataNodeProxy(String name, String description, ConfigMetadataNode delegate) {
        this.name = name;
        this.description = description;
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Type getType() {
        return delegate.getType();
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return delegate.accept(visitor);
    }
}
