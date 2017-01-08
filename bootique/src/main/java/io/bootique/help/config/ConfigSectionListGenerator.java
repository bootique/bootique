package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.meta.config.ConfigValueMetadata;

/**
 * @since 0.21
 */
class ConfigSectionListGenerator extends ConfigSectionGenerator {

    public ConfigSectionListGenerator(ConsoleAppender out) {
        super(out);
    }

    @Override
    protected void printNode(ConfigValueMetadata metadata, boolean asValue) {

        if (asValue) {
            String valueLabel = metadata.getType() != null ? sampleValue(metadata.getType()) : "?";
            out.println("- ", valueLabel);
        } else {
            out.println("-");
        }
    }
}
