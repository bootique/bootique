package io.bootique.help.config;

import io.bootique.module.ConfigMetadataNode;
import io.bootique.module.ConfigMetadataVisitor;
import io.bootique.module.ConfigObjectMetadata;
import io.bootique.module.ConfigPropertyMetadata;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.21
 */
class ConfigSectionListChildGenerator implements ConfigMetadataVisitor<Object> {

    private ConfigSectionGenerator parent;

    public ConfigSectionListChildGenerator(ConfigSectionGenerator parent) {
        this.parent = parent;
    }

    @Override
    public Object visitConfigPropertyMetadata(ConfigPropertyMetadata metadata) {
        printTypeHeader(metadata);

        String line = metadata.getType() != null ? parent.sampleValue(metadata.getType()) : "?";
        parent.printText("  ", line);
        return null;
    }

    @Override
    public Object visitConfigMetadata(ConfigObjectMetadata metadata) {
        printTypeHeader(metadata);

        List<ConfigMetadataNode> sortedChildren = metadata.getProperties()
                .stream()
                .sorted(Comparator.comparing(ConfigPropertyMetadata::getName))
                .collect(Collectors.toList());

        if (sortedChildren.isEmpty()) {
            return null;
        }

        ConfigMetadataNode last = sortedChildren.get(sortedChildren.size() - 1);

        ConfigSectionGenerator childGenerator = parent.withOffset(2);
        sortedChildren.forEach(p -> {
            p.accept(childGenerator);

            if (p != last) {
                parent.println();
            }
        });

        return null;
    }

    // TODO: visit list?

    protected void printTypeHeader(ConfigPropertyMetadata metadata) {
        Class<?> valueType = metadata.getType();

        if (valueType != null) {
            parent.printText("- # Element type: ", parent.typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            parent.printText("  # ", metadata.getDescription());
        }
    }
}
