package io.bootique.module;

import io.bootique.application.ApplicationMetadataNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Metadata descriptor of a DI module.
 *
 * @since 0.21
 */
public class ModuleMetadata extends ApplicationMetadataNode {

    private Collection<ConfigObjectMetadata> configs;

    private ModuleMetadata() {
        this.configs = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Collection<ConfigObjectMetadata> getConfigs() {
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

        public Builder addConfigs(Collection<ConfigObjectMetadata> configs) {
            moduleMetadata.configs.addAll(configs);
            return this;
        }
    }
}
