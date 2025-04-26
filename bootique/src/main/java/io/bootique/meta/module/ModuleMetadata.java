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

import io.bootique.BQModule;
import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Metadata descriptor of a single DI module.
 */
public class ModuleMetadata implements MetadataNode {

    // unwraps ConfigObjectMetadata decorated with ConfigMetadataNodeProxy
    private static final ConfigMetadataVisitor<Optional<ConfigObjectMetadata>> OBJECT_CONFIG_RESOLVER =
            new ConfigMetadataVisitor<>() {
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

    private final Class<? extends BQModule> type;
    private final String name;

    private final String description;
    private final boolean deprecated;
    private final Collection<ConfigMetadataNode> configs;

    private ModuleMetadata(
            Class<? extends BQModule> type,
            String name,
            String description,
            boolean deprecated,
            Collection<ConfigMetadataNode> configs) {

        this.type = type;
        this.name = name;
        this.description = description;
        this.deprecated = deprecated;
        this.configs = configs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    /**
     * Returns the Java class of the target module.
     *
     * @since 3.0
     */
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @since 3.0
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    public Collection<ConfigMetadataNode> getConfigs() {
        return configs;
    }

    /**
     * Locates configuration node for the specified dot-separated path.
     *
     * @param configPath a dot-separated path that presumably corresponds to a configuration node.
     * @return an optional result of a search for the node matching config path.
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

        if (configPath.isEmpty()) {
            return Optional.of(root);
        }

        String[] split = splitFirstComponent(configPath);

        return root.accept(new ConfigMetadataVisitor<>() {

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

        if (dot < 0) {
            // we need to strip array index if any
            int openBracketIndex = configPath.indexOf("[");
            return openBracketIndex < 0
                    ? new String[]{configPath, ""}
                    : new String[]{configPath.substring(0, openBracketIndex), ""};
        } else {
            return new String[]{
                    configPath.substring(0, dot),
                    configPath.substring(dot + 1)
            };
        }
    }

    public static class Builder {

        private Class<? extends BQModule> type;
        private String name;
        private String description;
        private boolean deprecated;
        private final Collection<ConfigMetadataNode> configs;

        private Builder() {
            this.configs = new ArrayList<>();
        }

        public ModuleMetadata build() {
            return new ModuleMetadata(
                    type != null ? type : BQModule.class,
                    name,
                    description,
                    deprecated,
                    configs);
        }

        /**
         * @since 3.0
         */
        public Builder type(Class<? extends BQModule> type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * @since 3.0
         */
        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder addConfig(ConfigObjectMetadata config) {
            this.configs.add(config);
            return this;
        }

        public Builder addConfigs(Collection<ConfigMetadataNode> configs) {
            this.configs.addAll(configs);
            return this;
        }
    }
}
