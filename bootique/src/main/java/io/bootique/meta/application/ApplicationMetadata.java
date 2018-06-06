/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.meta.application;

import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigValueMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Metadata object representing current application and its command-line interface.
 *
 * @since 0.20
 */
public class ApplicationMetadata implements MetadataNode {

    private String name;
    private String description;
    private Collection<CommandMetadata> commands;
    private Collection<OptionMetadata> options;
    private Collection<ConfigValueMetadata> variables;

    private ApplicationMetadata() {
        this.commands = new ArrayList<>();
        this.options = new ArrayList<>();
        this.variables = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder().defaultName();
    }

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Collection<CommandMetadata> getCommands() {
        return commands;
    }

    public Collection<OptionMetadata> getOptions() {
        return options;
    }

    /**
     * Returns a collection of metadata objects representing publicly exposed environment variables.
     *
     * @since 0.22
     * @return a collection of metadata objects representing publicly exposed environment variables.
     */
    public Collection<ConfigValueMetadata> getVariables() {
        return variables;
    }

    public static class Builder {

        private ApplicationMetadata application;

        private Builder() {
            this.application = new ApplicationMetadata();
        }

        public ApplicationMetadata build() {
            checkNameDuplicates(application.options);
            return application;
        }

        private void checkNameDuplicates(Collection<OptionMetadata> options) {
            if (options.size() > 1) {
                Set<String> distinctNames = new HashSet<>();
                options.forEach(om -> {
                    if (!distinctNames.add(om.getName())) {
                        throw new RuntimeException("Duplicate option declaration for '" + om.getName() + "'");
                    }
                });
            }
        }

        public Builder name(String name) {
            application.name = name;
            return this;
        }

        public Builder defaultName() {
            return name(ApplicationIntrospector.appNameFromRuntime());
        }

        public Builder description(String description) {
            application.description = description;
            return this;
        }

        public Builder addCommand(CommandMetadata commandMetadata) {
            application.commands.add(commandMetadata);
            return this;
        }

        public Builder addCommands(Collection<CommandMetadata> commandMetadata) {
            application.commands.addAll(commandMetadata);
            return this;
        }

        public Builder addOption(OptionMetadata option) {
            application.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<OptionMetadata> options) {
            application.options.addAll(options);
            return this;
        }

        public Builder addVariable(ConfigValueMetadata var) {
            application.variables.add(var);
            return this;
        }

        public Builder addVariables(Collection<ConfigValueMetadata> vars) {
            application.variables.addAll(vars);
            return this;
        }
    }

}
