package io.bootique.module;

import io.bootique.application.ApplicationMetadataNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata descriptor of a DI module.
 *
 * @since 0.21
 */
public class ModuleMetadata extends ApplicationMetadataNode {

    private Map<String, ConfigMetadata> configs;

    private ModuleMetadata() {
        this.configs = new HashMap<>();
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

    public Map<String, ConfigMetadata> getConfigs() {
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

        public Builder addConfig(String prefix, ConfigMetadata config) {
            moduleMetadata.configs.put(prefix, config);
            return this;
        }

        public Builder addConfigs(Map<String, ConfigMetadata> configs) {
            moduleMetadata.configs.putAll(configs);
            return this;
        }
    }
}
