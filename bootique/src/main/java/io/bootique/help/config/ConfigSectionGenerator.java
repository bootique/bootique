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
import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

class ConfigSectionGenerator implements ConfigMetadataVisitor<Object> {

    static final int DEFAULT_OFFSET = DefaultConfigHelpGenerator.DEFAULT_OFFSET;

    /**
     * Set to trace already printed types. It is used to break a possible recursion.
     */
    private final Set<Type> seenMetadataTypes;
    protected final ConsoleAppender out;

    public ConfigSectionGenerator(ConsoleAppender out, Set<Type> seenMetadataTypes) {
        this.out = Objects.requireNonNull(out);
        this.seenMetadataTypes = seenMetadataTypes;
    }

    @Override
    public Object visitObjectMetadata(ConfigObjectMetadata metadata) {
        printNode(metadata, false);

        List<ConfigObjectMetadata> selfAndSubconfigs = metadata
                .getAllSubConfigs()
                .map(md -> md.accept(new ConfigMetadataVisitor<ConfigObjectMetadata>() {
                    @Override
                    public ConfigObjectMetadata visitObjectMetadata(ConfigObjectMetadata visited) {

                        // Include the root type even if it has no properties.
                        // This ensures its header is printed in maps and lists
                        if (metadata == visited) {
                            return visited;
                        }

                        return visited.isAbstractType() || visited.getProperties().isEmpty() ? null : visited;
                    }
                }))
                .filter(Objects::nonNull)
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

        ConfigSectionListGenerator childGenerator = new ConfigSectionListGenerator(out.withOffset(DEFAULT_OFFSET), this.seenMetadataTypes);
        childGenerator.printListHeader(metadata);
        metadata.getElementType().accept(childGenerator);

        return null;
    }

    @Override
    public Object visitMapMetadata(ConfigMapMetadata metadata) {
        printNode(metadata, false);

        ConfigSectionMapGenerator childGenerator = new ConfigSectionMapGenerator(
                metadata.getKeysType(),
                out.withOffset(DEFAULT_OFFSET), this.seenMetadataTypes);

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

    protected void printObjectNoSubclasses(ConfigObjectMetadata metadata) {

        ConsoleAppender shifted = out.withOffset(DEFAULT_OFFSET);
        ConfigSectionGenerator childGenerator = new ConfigSectionGenerator(shifted, this.seenMetadataTypes);
        childGenerator.printObjectHeader(metadata);

        boolean willPrintProperties = seenMetadataTypes.add(metadata.getType())
                && !metadata.isAbstractType()
                && !metadata.getProperties().isEmpty();

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
                    .forEach(p -> p.accept(childGenerator));
        }
    }

    protected void printNode(ConfigValueMetadata metadata, boolean asValue) {

        if (asValue) {
            // value header goes on top of property name
            printValueHeader(metadata);
            String valueLabel = metadata.getValueLabel();
            out.println(metadata.getName(), ": ", valueLabel);
        } else {
            // headers for other types are printed below the property with the object contents
            out.println(metadata.getName(), ":");
        }
    }

    protected boolean isImpliedType(Type type) {
        String typeName = type.getTypeName();

        return switch (typeName) {
            case "boolean", "java.lang.Boolean",
                 "int", "java.lang.Integer",
                 "byte", "java.lang.Byte",
                 "double", "java.lang.Double",
                 "float", "java.lang.Float",
                 "short", "java.lang.Short",
                 "long", "java.lang.Long",
                 "java.lang.String",
                 "io.bootique.resource.ResourceFactory",
                 "io.bootique.resource.FolderResourceFactory" -> true;
            default -> false;
        };
    }

    protected String typeLabel(Type type) {

        String typeName = type.getTypeName();

        return switch (typeName) {
            case "java.lang.Boolean" -> "boolean";
            case "java.lang.Integer" -> "int";
            case "java.lang.Byte" -> "byte";
            case "java.lang.Double" -> "double";
            case "java.lang.Float" -> "float";
            case "java.lang.Short" -> "short";
            case "java.lang.Long" -> "long";
            case "java.lang.String" -> "String";
            default -> switch (type) {
                case Class c when Map.class.isAssignableFrom(c) -> "Map";
                // TODO: decipher collection type... for now hardcoding List type
                case Class c when Collection.class.isAssignableFrom(c) -> "List";
                case ParameterizedType pt -> parameterizedTypeLabel(pt);
                default -> typeName;
            };
        };
    }

    private String parameterizedTypeLabel(ParameterizedType type) {
        String rawTypeLabel = typeLabel(type.getRawType());
        String paramsLabel =
                Arrays.stream(type.getActualTypeArguments())
                        .map(this::typeLabel)
                        .collect(Collectors.joining(", ", "<", ">"));

        return rawTypeLabel + paramsLabel;
    }
}
