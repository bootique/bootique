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

        switch (typeName) {
            case "boolean":
            case "java.lang.Boolean":
                return "<true|false>";
            case "int":
            case "java.lang.Integer":
                return "<int>";
            case "byte":
            case "java.lang.Byte":
                return "<byte>";
            case "double":
            case "java.lang.Double":
                return "<double>";
            case "float":
            case "java.lang.Float":
                return "<float>";
            case "short":
            case "java.lang.Short":
                return "<short>";
            case "long":
            case "java.lang.Long":
                return "<long>";
            case "char":
            case "java.lang.Character":
                return "<char>";
            case "java.lang.String":
                return "<string>";
            case "io.bootique.resource.ResourceFactory":
                return "<resource-uri>";
            case "io.bootique.resource.FolderResourceFactory":
                return "<folder-resource-uri>";
            default:
                if (type instanceof Class) {
                    Class<?> classType = (Class<?>) type;
                    if (classType.isEnum()) {

                        StringBuilder out = new StringBuilder("<");
                        Object[] values = classType.getEnumConstants();
                        for (int i = 0; i < values.length; i++) {
                            if (i > 0) {
                                out.append("|");
                            }
                            out.append(values[i]);
                        }
                        out.append(">");
                        return out.toString();
                    }

                }

                return "<value>";
        }
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
            if (valueLabel != null && valueLabel.length() == 0) {
                valueLabel = null;
            }

            toBuild.valueLabel = valueLabel;

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

        public B unbound() {
            toBuild.unbound = true;
            return (B) this;
        }
    }
}
