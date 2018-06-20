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

import java.lang.reflect.Type;

/**
 * Descriptor of a configuration value property.
 *
 * @since 0.21
 */
public class ConfigValueMetadata implements ConfigMetadataNode {

    protected Type type;
    protected String name;
    protected String description;

    protected ConfigValueMetadata() {
    }

    public static Builder builder() {
        return new Builder(new ConfigValueMetadata());
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

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitValueMetadata(this);
    }

    @Override
    public Type getType() {
        return type;
    }

    // parameterization is needed to enable covariant return types in subclasses
    public static class Builder<T extends ConfigValueMetadata, B extends Builder<T, B>> {

        protected T toBuild;

        protected Builder(T toBuild) {
            this.toBuild = toBuild;
        }

        public T build() {
            return toBuild;
        }

        public B name(String name) {
            toBuild.name = name;
            return (B) this;
        }

        public B description(String description) {
            if (description != null && description.length() == 0) {
                description = null;
            }

            toBuild.description = description;
            return (B) this;
        }

        public B type(Type type) {
            toBuild.type = type;
            return (B) this;
        }
    }
}
