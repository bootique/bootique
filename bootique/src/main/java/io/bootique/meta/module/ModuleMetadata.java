/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.meta.module;

import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigMetadataVisitor;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Metadata descriptor of a DI module.
 *
 * @since 0.21
 */
public class ModuleMetadata implements MetadataNode {

    // unwraps ConfigObjectMetadata decorated with ConfigMetadataNodeProxy
    private static final ConfigMetadataVisitor<Optional<ConfigObjectMetadata>> OBJECT_CONFIG_RESOLVER =
            new ConfigMetadataVisitor<Optional<ConfigObjectMetadata>>() {
                @Override
                public Optional<ConfigObjectMetadata> visitObjectMetadata(ConfigObjectMetadata metadata) {
                    return Optional.of(metadata);
                }

                @Override
                public Optional<ConfigObjectMetadata> visitValueMetadata(ConfigValueMetadata metadata) {
                    return Optional.empty();
                }

                @Override
                public Optional<ConfigObjectMetadata> visitListMetadata(ConfigListMetadata metadata) {
                    return Optional.empty();
                }

                @Override
                public Optional<ConfigObjectMetadata> visitMapMetadata(ConfigMapMetadata metadata) {
                    return Optional.empty();
                }
            };

    private String name;
    private String providerName;
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

    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Collection<ConfigMetadataNode> getConfigs() {
        return configs;
    }

    /**
     * Locates configuration node for the specified dot-separated path.
     *
     * @param configPath a dot-separated path that presumably corresponds to a configuration node.
     * @return an optional result of a search for the node matching config path.
     * @since 0.22
     */
    public Optional<ConfigMetadataNode> findConfig(String configPath) {

        String[] split = splitFirstComponent(configPath);

        for (ConfigMetadataNode c : configs) {
            if (c.getName().equals(split[0])) {
                return findConfig(c, split[1]);
            }
        }

        return Optional.empty();
    }

    protected Optional<ConfigMetadataNode> findConfig(ConfigMetadataNode root, String configPath) {
        Objects.requireNonNull(root);

        if (configPath.length() == 0) {
            return Optional.of(root);
        }

        String[] split = splitFirstComponent(configPath);

        return root.accept(new ConfigMetadataVisitor<Optional<ConfigMetadataNode>>() {

            @Override
            public Optional<ConfigMetadataNode> visitObjectMetadata(ConfigObjectMetadata metadata) {

                return metadata.getAllSubConfigs()
                        .map(c -> c.accept(OBJECT_CONFIG_RESOLVER))
                        .filter(Optional::isPresent)
                        .map(c -> c.get().getProperties())
                        .flatMap(Collection::stream)
                        .filter(c -> c.getName().equals(split[0]))
                        .map(c -> findConfig(c, split[1]))
                        .findFirst().orElse(Optional.empty());
            }

            @Override
            public Optional<ConfigMetadataNode> visitMapMetadata(ConfigMapMetadata metadata) {

                // map can have arbitrary keys, so any name is valid
                return findConfig(metadata.getValuesType(), split[1]);
            }

            @Override
            public Optional<ConfigMetadataNode> visitValueMetadata(ConfigValueMetadata metadata) {
                return Optional.empty();
            }

            @Override
            public Optional<ConfigMetadataNode> visitListMetadata(ConfigListMetadata metadata) {
                return Optional.empty();
            }
        });
    }

    private String[] splitFirstComponent(String configPath) {
        int dot = configPath.indexOf('.');

        return dot < 0 ? new String[]{configPath, ""} : new String[]{
                configPath.substring(0, dot),
                configPath.substring(dot + 1)
        };
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

        public Builder providerName(String name) {
            moduleMetadata.providerName = name;
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
