package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.meta.config.ConfigValueMetadata;

import java.lang.reflect.Type;

/**
 * @since 0.21
 */
class ConfigSectionListGenerator extends ConfigSectionGenerator {

    public ConfigSectionListGenerator(ConsoleAppender out) {
        super(out);
    }

    @Override
    protected void printNode(ConfigValueMetadata metadata, boolean asValue) {
        Type valueType = metadata.getType();

        boolean dash = false;
        if (valueType != null && !isImpliedType(valueType)) {
            dash = true;
            out.println("- # Type: ", typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            out.println(dash ? "  # " : "- # ", metadata.getDescription());
            dash = true;
        }

        if (asValue) {
            String valueLabel = metadata.getType() != null ? sampleValue(metadata.getType()) : "?";
            out.println(dash ? "  " : "- ", valueLabel);
        }
    }
}
