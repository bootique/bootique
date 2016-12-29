package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.help.HelpAppender;
import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @since 0.21
 */
public class DefaultConfigHelpGenerator implements ConfigHelpGenerator {

    static final int DEFAULT_OFFSET = 6;

    private ModulesMetadata modulesMetadata;
    private int lineWidth;

    public DefaultConfigHelpGenerator(ModulesMetadata modulesMetadata, int lineWidth) {
        this.lineWidth = lineWidth;
        this.modulesMetadata = modulesMetadata;
    }

    protected HelpAppender createAppender(Appendable out) {
        return new HelpAppender(createConsoleAppender(out));
    }

    protected ConsoleAppender createConsoleAppender(Appendable out) {
        return new ConsoleAppender(out, lineWidth);
    }

    @Override
    public void append(Appendable out) {
        HelpAppender appender = createAppender(out);

        List<ModuleMetadata> sortedModules = modulesMetadata
                .getModules()
                .stream()
                .sorted(Comparator.comparing(ModuleMetadata::getName))
                .collect(Collectors.toList());

        printModules(appender, sortedModules);

        List<ConfigMetadataNode> sortedConfigs = sortedModules.stream()
                .map(ModuleMetadata::getConfigs)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(MetadataNode::getName))
                .collect(Collectors.toList());

        printConfigurations(appender, sortedConfigs);
    }

    protected void printModules(HelpAppender out, Collection<ModuleMetadata> modules) {

        if (modules.isEmpty()) {
            return;
        }

        out.printSectionName("MODULES");
        modules.forEach(m -> {
            printModuleName(out, m.getName(), m.getDescription());
        });
    }

    protected void printConfigurations(HelpAppender out, List<ConfigMetadataNode> configs) {

        if (configs.isEmpty()) {
            return;
        }

        out.printSectionName("CONFIGURATION");

        // using the underlying appender for config section body. Unlike HelpAppender it allows
        // controlling any number of nested offsets
        ConfigSectionGenerator generator = new ConfigSectionGenerator(out.getAppender().withOffset(DEFAULT_OFFSET));
        ConfigMetadataNode last = configs.get(configs.size() - 1);

        configs.forEach(c -> {
            printConfiguration(generator, c);

            if (c != last) {
                out.println();
            }
        });
    }

    protected void printModuleName(HelpAppender out, String moduleName, String description) {
        Objects.requireNonNull(moduleName);

        if (description != null) {
            out.printSubsectionHeader(moduleName, ": ", description);
        } else {
            out.printSubsectionHeader(moduleName);
        }
    }

    protected void printConfiguration(ConfigSectionGenerator generator, ConfigMetadataNode node) {
        node.accept(generator);
    }
}
