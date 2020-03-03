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
import io.bootique.help.HelpAppender;
import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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

    /**
     * @param out Appendable that will be used to append data to
     * @param predicate The predicate to configure stream filter
     * @since 2.0
     */
    @Override
    public void append(Appendable out, Predicate<MetadataNode> predicate) {
        HelpAppender appender = createAppender(out);

        List<ModuleMetadata> sortedModules = modulesMetadata
                .getModules()
                .stream()
                .sorted(Comparator.comparing(ModuleMetadata::getName))
                .collect(Collectors.toList());

        printModules(appender, sortedModules);

        List<ConfigMetadataNode> sortedConfigs = sortedModules
                .stream()
                .map(ModuleMetadata::getConfigs)
                .flatMap(Collection::stream)
                .filter(predicate)
                .sorted(Comparator.comparing(MetadataNode::getName))
                .collect(Collectors.toList());

        printConfigurations(appender, sortedConfigs);
    }

    protected void printModules(HelpAppender out, Collection<ModuleMetadata> modules) {

        if (modules.isEmpty()) {
            return;
        }

        out.printSectionName("MODULES");
        modules.forEach(m -> printModuleName(out, m.getName(), m.getDescription()));
    }

    protected void printConfigurations(HelpAppender out, List<ConfigMetadataNode> configs) {

        if (configs.isEmpty()) {
            return;
        }

        out.printSectionName("CONFIGURATION");

        // using the underlying appender for config section body. Unlike HelpAppender it allows
        // controlling any number of nested offsets
        ConfigSectionGenerator generator = new ConfigSectionGenerator(out.getAppender().withOffset(DEFAULT_OFFSET), new HashSet<>());
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
