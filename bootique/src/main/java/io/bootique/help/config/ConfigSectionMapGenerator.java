package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.meta.config.ConfigValueMetadata;

import java.lang.reflect.Type;
import java.util.Objects;

public class ConfigSectionMapGenerator extends ConfigSectionGenerator {

    private Class<?> keysType;

    public ConfigSectionMapGenerator(Class<?> keysType, ConsoleAppender out) {
        super(out);
        this.keysType = Objects.requireNonNull(keysType);
    }

    @Override
    protected void printNode(ConfigValueMetadata metadata, boolean asValue) {

        Type valueType = metadata.getType();
        if (valueType != null) {
            out.println("# Type: ", typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            out.println("# ", metadata.getDescription());
        }

        if (asValue) {
            String valueLabel = metadata.getType() != null ? sampleValue(metadata.getType()) : "?";
            out.println(sampleValue(keysType), ": ", valueLabel);
        } else {
            out.println(sampleValue(keysType), ":");
        }
    }
}
