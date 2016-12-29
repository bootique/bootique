package io.bootique.help.config;

import io.bootique.meta.MetadataNode;
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
        printTypeHeader(metadata);

        String keyLabel = parent.sampleValue(keysType);
        String line = metadata.getType() != null ? parent.sampleValue(metadata.getType()) : "?";
        parent.printText(keyLabel, ": ", line);
        return null;
    }

    @Override
    public Object visitObjectMetadata(ConfigObjectMetadata metadata) {
        printTypeHeader(metadata);

        String keyLabel = parent.sampleValue(keysType);
        parent.printText(keyLabel, ":");

        List<ConfigMetadataNode> sortedChildren = metadata.getProperties()
                .stream()
                .sorted(Comparator.comparing(MetadataNode::getName))
                .collect(Collectors.toList());

        if (sortedChildren.isEmpty()) {
            return null;
        }

        ConfigMetadataNode last = sortedChildren.get(sortedChildren.size() - 1);

        ConfigSectionGenerator childGenerator = parent.withOffset();
        sortedChildren.forEach(p -> {
            p.accept(childGenerator);

            if (p != last) {
                parent.println();
            }
        });

        return null;
    }

    // TODO: visit list and map...

    protected void printTypeHeader(ConfigValueMetadata metadata) {

        parent.printText("# Keys type: ", parent.typeLabel(keysType));

        Type valueType = metadata.getType();
        if (valueType != null) {
            parent.printText("# Values type: ", parent.typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            parent.printText("# ", metadata.getDescription());
        }
    }
}
