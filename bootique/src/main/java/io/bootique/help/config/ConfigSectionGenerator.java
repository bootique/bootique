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

package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.help.ValueObjectDescriptor;
import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigMetadataVisitor;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @since 0.21
 */
class ConfigSectionGenerator implements ConfigMetadataVisitor<Object> {

    static final int DEFAULT_OFFSET = DefaultConfigHelpGenerator.DEFAULT_OFFSET;

    protected Map<Class<?>, ValueObjectDescriptor> valueObjectsDescriptors;
    protected ConsoleAppender out;

    public ConfigSectionGenerator(ConsoleAppender out, Map<Class<?>, ValueObjectDescriptor> valueObjectsDescriptors) {
        this.out = Objects.requireNonNull(out);
        this.valueObjectsDescriptors = valueObjectsDescriptors;
    }

    @Override
    public Object visitObjectMetadata(ConfigObjectMetadata metadata) {
        printNode(metadata, false);

        List<ConfigObjectMetadata> selfAndSubconfigs = metadata
                .getAllSubConfigs()
                .map(md -> md.accept(new ConfigMetadataVisitor<ConfigObjectMetadata>() {
                    @Override
                    public ConfigObjectMetadata visitObjectMetadata(ConfigObjectMetadata visited) {

                        // include the root type even if it has no properties.. This ensure its header is printed in
                        // maps and lists
                        if (metadata == visited) {
                            return visited;
                        }

                        return visited.isAbstractType() || visited.getProperties().isEmpty() ? null : visited;
                    }
                }))
                .filter(md -> md != null)
                .collect(Collectors.toList());

        if (!selfAndSubconfigs.isEmpty()) {
            ConfigObjectMetadata last = selfAndSubconfigs.get(selfAndSubconfigs.size() - 1);
            selfAndSubconfigs.forEach(md -> {
                printObjectNoSubclasses(md);

                if (md != last) {
                    out.println();
                }
            });
        }

        return null;
    }

    @Override
    public Object visitValueMetadata(ConfigValueMetadata metadata) {
        printNode(metadata, true);
        return null;
    }

    @Override
    public Object visitListMetadata(ConfigListMetadata metadata) {
        printNode(metadata, false);

        ConfigSectionListGenerator childGenerator = new ConfigSectionListGenerator(out.withOffset(DEFAULT_OFFSET), valueObjectsDescriptors);
        childGenerator.printListHeader(metadata);
        metadata.getElementType().accept(childGenerator);

        return null;
    }

    @Override
    public Object visitMapMetadata(ConfigMapMetadata metadata) {
        printNode(metadata, false);

        ConfigSectionMapGenerator childGenerator = new ConfigSectionMapGenerator(
                metadata.getKeysType(),
                out.withOffset(DEFAULT_OFFSET),
                this.valueObjectsDescriptors);

        childGenerator.printMapHeader(metadata);
        metadata.getValuesType().accept(childGenerator);
        return null;
    }

    protected void printValueHeader(ConfigValueMetadata metadata) {

        if (metadata.getDescription() != null) {
            out.withOffset("# ").foldPrintln(metadata.getDescription());
        }

        Type valueType = metadata.getType();
        if (valueType != null && !isImpliedType(valueType)) {
            out.withOffset("# ").foldPrintln("Resolved as '", typeLabel(valueType), "'.");
        }
    }

    protected void printObjectHeader(ConfigObjectMetadata metadata) {

        out.println("#");

        if (metadata.getTypeLabel() != null) {
            out.println("# Type option: ", metadata.getTypeLabel());
        }

        printValueHeader(metadata);
        out.println("#");
    }

    protected void printMapHeader(ConfigMapMetadata metadata) {
        out.println("#");
        printValueHeader(metadata);
        out.println("#");
    }

    protected void printListHeader(ConfigListMetadata metadata) {

        out.println("#");
        printValueHeader(metadata);
        out.println("#");
    }

    protected void printMapHeader(ConfigMetadataNode metadata, boolean padded) {

        if (padded) {
            out.println("#");
        }

        String typeLabel = metadata.accept(new ConfigMetadataVisitor<String>() {
            @Override
            public String visitObjectMetadata(ConfigObjectMetadata metadata) {
                return metadata.getTypeLabel();
            }
        });

        if (typeLabel != null) {
            out.println("# Type option: ", typeLabel);
        }

        if (metadata.getDescription() != null) {
            out.println("# ", metadata.getDescription());
        }

        Type valueType = metadata.getType();
        if (valueType != null && !isImpliedType(valueType)) {
            out.println("# Resolved as '", typeLabel(valueType), "'.");
        }

        if (padded) {
            out.println("#");
        }
    }

    protected void printObjectNoSubclasses(ConfigObjectMetadata metadata) {

        ConsoleAppender shifted = out.withOffset(DEFAULT_OFFSET);
        ConfigSectionGenerator childGenerator = new ConfigSectionGenerator(shifted, valueObjectsDescriptors);
        childGenerator.printObjectHeader(metadata);

        boolean willPrintProperties = !metadata.isAbstractType() && !metadata.getProperties().isEmpty();
        boolean willPrintType = metadata.getTypeLabel() != null;

        if (willPrintProperties || willPrintType) {
            shifted.println();
        }

        if (willPrintType) {
            shifted.println("type: '", metadata.getTypeLabel() + "'");
        }

        if (willPrintProperties) {
            metadata.getProperties()
                    .stream()
                    .sorted(Comparator.comparing(MetadataNode::getName))
                    .forEach(p -> {
                        p.accept(childGenerator);
                    });
        }
    }

    protected void printNode(ConfigValueMetadata metadata, boolean asValue) {

        if (asValue) {
            // value header goes on top of property name
            printValueHeader(metadata);
            String valueLabel = metadata.getType() != null ? sampleValue(metadata.getType()) : "?";
            out.println(metadata.getName(), ": ", valueLabel);
        } else {
            // headers for other types are printed below the property with the object contents
            out.println(metadata.getName(), ":");
        }
    }

    protected String sampleValue(Type type) {

        // TODO: allow to provide sample values in metadata, so that we can display something useful

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

                    if (valueObjectsDescriptors != null && valueObjectsDescriptors.containsKey(type)) {
                    	return new StringBuilder("<").append(valueObjectsDescriptors.get(type)).append(">").toString();
					}
                }

                return "<value>";
        }
    }

    protected boolean isImpliedType(Type type) {
        String typeName = type.getTypeName();

        switch (typeName) {
            case "boolean":
            case "java.lang.Boolean":
            case "int":
            case "java.lang.Integer":
            case "byte":
            case "java.lang.Byte":
            case "double":
            case "java.lang.Double":
            case "float":
            case "java.lang.Float":
            case "short":
            case "java.lang.Short":
            case "long":
            case "java.lang.Long":
            case "java.lang.String":
            case "io.bootique.resource.ResourceFactory":
            case "io.bootique.resource.FolderResourceFactory":
                return true;
            default:
                return false;
        }
    }

    protected String typeLabel(Type type) {

        String typeName = type.getTypeName();

        switch (typeName) {
            case "java.lang.Boolean":
                return "boolean";
            case "java.lang.Integer":
                return "int";
            case "java.lang.Byte":
                return "byte";
            case "java.lang.Double":
                return "double";
            case "java.lang.Float":
                return "float";
            case "java.lang.Short":
                return "short";
            case "java.lang.Long":
                return "long";
            case "java.lang.String":
                return "String";
            default:

                if (type instanceof Class) {
                    Class<?> classType = (Class<?>) type;
                    if (Map.class.isAssignableFrom(classType)) {
                        return "Map";
                    }
                    // TODO: decipher collection type... for now hardcoding List type
                    else if (Collection.class.isAssignableFrom(classType)) {
                        return "List";
                    }
                } else if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;

                    StringBuilder out = new StringBuilder(typeLabel(parameterizedType.getRawType()));
                    out.append("<");

                    Type[] args = parameterizedType.getActualTypeArguments();
                    if (args != null) {
                        for (int i = 0; i < args.length; i++) {
                            if (i > 0) {
                                out.append(", ");
                            }
                            out.append(typeLabel(args[i]));
                        }
                    }

                    out.append(">");
                    return out.toString();
                }

                return typeName;
        }
    }
}
