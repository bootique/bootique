package io.bootique.help.config;

import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigMetadataVisitor;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigSectionMapChildGenerator implements ConfigMetadataVisitor<Object> {

    private ConfigSectionGenerator parent;
    private Class<?> keysType;

    public ConfigSectionMapChildGenerator(Class<?> keysType, ConfigSectionGenerator parent) {
        this.parent = parent;
        this.keysType = Objects.requireNonNull(keysType);
    }

    @Override
    public Object visitValueMetadata(ConfigValueMetadata metadata) {
        printTypeHeader(metadata, true);
        return null;
    }

    @Override
    public Object visitObjectMetadata(ConfigObjectMetadata metadata) {
        printTypeHeader(metadata, false);

        List<ConfigMetadataNode> sortedChildren = metadata.getProperties()
                .stream()
                .sorted(Comparator.comparing(MetadataNode::getName))
                .collect(Collectors.toList());

        if (sortedChildren.isEmpty()) {
            return null;
        }

        ConfigMetadataNode last = sortedChildren.get(sortedChildren.size() - 1);

        ConfigSectionGenerator childGenerator = parent.withOffset(ConfigSectionGenerator.DEFAULT_OFFSET);
        sortedChildren.forEach(p -> {
            p.accept(childGenerator);

            if (p != last) {
                parent.println();
            }
        });

        return null;
    }

    @Override
    public Object visitMapMetadata(ConfigMapMetadata metadata) {
        printTypeHeader(metadata, false);

        // TODO: should support multiple element types (from META-INF/services/PolymorphicConfiguration)
        ConfigSectionGenerator childGenerator = parent.withOffset(ConfigSectionGenerator.DEFAULT_OFFSET);
        metadata.getValuesType().accept(
                new ConfigSectionMapChildGenerator(metadata.getKeysType(), childGenerator));

        return null;
    }

    @Override
    public Object visitListMetadata(ConfigListMetadata metadata) {
        printTypeHeader(metadata, false);

        // TODO: should support multiple element types (from META-INF/services/PolymorphicConfiguration)
        ConfigSectionGenerator childGenerator = parent.withOffset(ConfigSectionGenerator.DEFAULT_OFFSET);
        metadata.getElementType().accept(new ConfigSectionListChildGenerator(childGenerator));

        return null;
    }

    protected void printTypeHeader(ConfigValueMetadata metadata, boolean asValue) {

        parent.println("# Keys type: ", parent.typeLabel(keysType));

        Type valueType = metadata.getType();
        if (valueType != null) {
            parent.println("# Values type: ", parent.typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            parent.println("# ", metadata.getDescription());
        }

        if (asValue) {
            String valueLabel = metadata.getType() != null ? parent.sampleValue(metadata.getType()) : "?";
            parent.println(parent.sampleValue(keysType), ": ", valueLabel);
        } else {
            parent.println(parent.sampleValue(keysType), ":");
        }
    }
}
