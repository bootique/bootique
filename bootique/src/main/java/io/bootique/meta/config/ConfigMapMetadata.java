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

package io.bootique.meta.config;

import java.util.Map;
import java.util.Objects;

/**
 * @since 0.21
 */
public class ConfigMapMetadata extends ConfigValueMetadata {

    private Class<?> keysType;
    private ConfigMetadataNode valuesType;

    public static Builder builder() {
        return new Builder(new ConfigMapMetadata()).type(Map.class);
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitMapMetadata(this);
    }

    public Class<?> getKeysType() {
        return keysType;
    }

    public ConfigMetadataNode getValuesType() {
        return valuesType;
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigMapMetadata, ConfigMapMetadata.Builder> {

        public Builder(ConfigMapMetadata toBuild) {
            super(toBuild);
        }

        @Override
        public ConfigMapMetadata build() {
            Objects.requireNonNull(toBuild.valuesType);
            return super.build();
        }

        public Builder keysType(Class<?> keysType) {
            toBuild.keysType = keysType;
            return this;
        }

        public Builder valuesType(ConfigMetadataNode elementType) {
            toBuild.valuesType = elementType;
            return this;
        }
    }
}
