package io.bootique.help.config;

import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigMetadataVisitor;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;

import java.lang.reflect.Type;
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
    public Object visitConfigPropertyMetadata(ConfigValueMetadata metadata) {
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
                .sorted(Comparator.comparing(MetadataNode::getName))
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

    protected void printTypeHeader(ConfigValueMetadata metadata) {
        Type valueType = metadata.getType();

        if (valueType != null) {
            parent.printText("- # Element type: ", parent.typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            parent.printText("  # ", metadata.getDescription());
        }
    }
}
