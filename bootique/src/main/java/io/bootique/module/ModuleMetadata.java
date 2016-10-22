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

    private Collection<ConfigMetadata> configs;

    private ModuleMetadata() {
        this.configs = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    public Collection<ConfigMetadata> getConfigs() {
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

        public Builder addConfig(ConfigMetadata config) {
            moduleMetadata.configs.add(config);
            return this;
        }

        public Builder addConfigs(Collection<ConfigMetadata> configs) {
            moduleMetadata.configs.addAll(configs);
            return this;
        }
    }
}
