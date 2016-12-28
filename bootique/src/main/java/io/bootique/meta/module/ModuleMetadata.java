package io.bootique.meta.module;

import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Metadata descriptor of a DI module.
 *
 * @since 0.21
 */
public class ModuleMetadata implements MetadataNode {

    private String name;
    private String description;
    private Collection<ConfigMetadataNode> configs;

    private ModuleMetadata() {
        this.configs = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Collection<ConfigMetadataNode> getConfigs() {
        return configs;
    }

    public static class Builder {

        private ModuleMetadata moduleMetadata;

        private Builder() {
            this.moduleMetadata = new ModuleMetadata();
        }

        public ModuleMetadata build() {
            return moduleMetadata;
        }

        public Builder name(String name) {
            moduleMetadata.name = name;
            return this;
        }

        public Builder description(String description) {
            moduleMetadata.description = description;
            return this;
        }

        public Builder addConfig(ConfigObjectMetadata config) {
            moduleMetadata.configs.add(config);
            return this;
        }

        public Builder addConfigs(Collection<ConfigMetadataNode> configs) {
            moduleMetadata.configs.addAll(configs);
            return this;
        }
    }
}
