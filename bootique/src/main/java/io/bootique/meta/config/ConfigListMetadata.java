/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.meta.config;

import java.util.List;
import java.util.Objects;

/**
 * @since 0.21
 */
public class ConfigListMetadata extends ConfigValueMetadata {

    private ConfigMetadataNode elementType;

    public static Builder builder() {
        return new Builder(new ConfigListMetadata()).type(List.class);
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitListMetadata(this);
    }

    public ConfigMetadataNode getElementType() {
        return elementType;
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigListMetadata, ConfigListMetadata.Builder> {

        public Builder(ConfigListMetadata toBuild) {
            super(toBuild);
        }

        @Override
        public ConfigListMetadata build() {
            Objects.requireNonNull(toBuild.elementType);
            return super.build();
        }

        public Builder elementType(ConfigMetadataNode elementType) {
            toBuild.elementType = elementType;
            return this;
        }
    }
}
