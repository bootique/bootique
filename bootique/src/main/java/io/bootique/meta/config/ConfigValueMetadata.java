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
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Descriptor of a configuration value property.
 */
public class ConfigValueMetadata implements ConfigMetadataNode {

    protected Type type;
    protected String name;
    protected String description;
    protected String valueLabel;
    protected boolean unbound;

    protected ConfigValueMetadata() {
    }

    public static Builder builder() {
        return new Builder<>(new ConfigValueMetadata());
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isUnbound() {
        return unbound;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getValueLabel() {

        if (valueLabel != null) {
            return "<" + valueLabel + ">";
        }

        if (type == null) {
            return "?";
        }

        return getSampleValue(type);
    }

    public String getSampleValue(Type type) {

        String typeName = type.getTypeName();

        return switch (typeName) {
            case "boolean", "java.lang.Boolean" -> "<true|false>";
            case "int", "java.lang.Integer" -> "<int>";
            case "byte", "java.lang.Byte" -> "<byte>";
            case "double", "java.lang.Double" -> "<double>";
            case "float", "java.lang.Float" -> "<float>";
            case "short", "java.lang.Short" -> "<short>";
            case "long", "java.lang.Long" -> "<long>";
            case "char", "java.lang.Character" -> "<char>";
            case "java.lang.String" -> "<string>";
            case "io.bootique.resource.ResourceFactory" -> "<resource-uri>";
            case "io.bootique.resource.FolderResourceFactory" -> "<folder-resource-uri>";
            default -> (type instanceof Class c && c.isEnum())
                    ? Arrays.stream(c.getEnumConstants()).map(Object::toString).collect(Collectors.joining("|", "<", ">"))
                    : "<value>";
        };
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

        /**
         * @since 3.0
         */
        public B from(T copyFrom) {

            toBuild.name = copyFrom.name;
            toBuild.description = copyFrom.description;
            toBuild.valueLabel = copyFrom.valueLabel;
            toBuild.type = copyFrom.type;

            return (B) this;
        }

        public B name(String name) {
            toBuild.name = name;
            return (B) this;
        }

        public B valueLabel(String valueLabel) {
            if (valueLabel != null && valueLabel.isEmpty()) {
                valueLabel = null;
            }

            toBuild.valueLabel = valueLabel;

            return (B) this;
        }

        public B description(String description) {
            if (description != null && description.isEmpty()) {
                description = null;
            }

            toBuild.description = description;
            return (B) this;
        }

        public B type(Type type) {
            toBuild.type = type;
            return (B) this;
        }

        public B unbound() {
            toBuild.unbound = true;
            return (B) this;
        }
    }
}
