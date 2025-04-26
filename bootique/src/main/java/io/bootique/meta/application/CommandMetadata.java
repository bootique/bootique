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

package io.bootique.meta.application;

import io.bootique.command.Command;
import io.bootique.meta.MetadataNode;
import io.bootique.names.ClassToName;

import java.util.ArrayList;
import java.util.Collection;

public class CommandMetadata implements MetadataNode {

    private boolean hidden;
    private final Collection<OptionMetadata> options;
    private OptionMetadata commandOption;

    public CommandMetadata() {
        this.options = new ArrayList<>();
    }

    /**
     * Returns {@link OptionMetadata} for this command CLI flag.
     *
     * @since 3.0
     */
    public OptionMetadata getCommandOption() {
        return commandOption;
    }

    /**
     * Creates command metadata based on the command class, applying default naming algorithm. If you need to provide
     * custom metadata parameters, such as description, use {@link #builder(Class)} instead.
     *
     * @since 3.0
     */
    public static CommandMetadata of(Class<? extends Command> commandType) {
        return new Builder().commandType(commandType).build();
    }

    /**
     * Creates command metadata with provided command name. If you need to provide more custom metadata parameters,
     * such as description, use {@link #builder(String)} instead.
     *
     * @since 3.0
     */
    public static CommandMetadata of(String commandName) {
        return new Builder().name(commandName).build();
    }

    public static Builder builder(Class<? extends Command> commandType) {
        return new Builder().commandType(commandType);
    }

    public static Builder builder(String commandName) {
        return new Builder().name(commandName);
    }

    @Override
    public String getName() {
        return commandOption.getName();
    }

    @Override
    public String getDescription() {
        return commandOption.getDescription();
    }

    /**
     * Returns extra options recognized this command. The main option that activates the command is not included in
     * this collection and is accessible via {@link #getCommandOption()}.
     */
    public Collection<OptionMetadata> getOptions() {
        return options;
    }

    /**
     * Returns whether the command should be hidden by default. Ultimately {@link io.bootique.command.CommandManager}
     * defines whether any given command is public or hidden. This property defines the default policy for the given
     * command.
     *
     * @return whether the command should be hidden by default.
     */
    public boolean isHidden() {
        return hidden;
    }

    public static class Builder {

        private static final ClassToName NAME_BUILDER = ClassToName
                .builder()
                .convertToLowerCase()
                .partsSeparator("-")
                .stripSuffix("Command")
                .build();

        private final CommandMetadata metadata;
        private final OptionMetadata.Builder optionBuilder;

        private Builder() {
            this.metadata = new CommandMetadata();
            this.optionBuilder = OptionMetadata.builder();
        }

        public CommandMetadata build() {
            metadata.commandOption = optionBuilder.build();
            return metadata;
        }

        public Builder commandType(Class<? extends Command> commandType) {
            optionBuilder.name(NAME_BUILDER.toName(commandType));
            return this;
        }

        public Builder name(String name) {
            optionBuilder.name(name);
            return this;
        }

        public Builder shortName(char shortName) {
            optionBuilder.shortName(shortName);
            return this;
        }

        public Builder description(String description) {
            optionBuilder.description(description);
            return this;
        }

        /**
         * @since 3.0
         */
        public CommandMetadata.Builder valueRequired() {
            return valueRequired("");
        }

        /**
         * @since 3.0
         */
        public CommandMetadata.Builder valueRequired(String valueName) {
            optionBuilder.valueRequired(valueName);
            return this;
        }

        /**
         * @since 3.0
         */
        public CommandMetadata.Builder valueOptional() {
            optionBuilder.valueOptional();
            return this;
        }

        /**
         * @since 3.0
         */
        public CommandMetadata.Builder valueOptional(String valueName) {
            optionBuilder.valueOptional(valueName);
            return this;
        }

        /**
         * Marks value optional and sets the default value for this command that will be used if the command is provided on
         * command line without an explicit value.
         *
         * @param defaultValue a default value for the command.
         * @return this builder instance
         * @since 3.0
         */
        public CommandMetadata.Builder valueOptionalWithDefault(String defaultValue) {
            optionBuilder.valueOptionalWithDefault(defaultValue);
            return this;
        }

        /**
         * Marks value optional and sets the default value for this command that will be used if the command is provided on
         * command line without an explicit value.
         *
         * @param valueName    a description of value
         * @param defaultValue a default value for the option.
         * @return this builder instance
         */
        public CommandMetadata.Builder valueOptionalWithDefault(String valueName, String defaultValue) {
            optionBuilder.valueOptionalWithDefault(valueName, defaultValue);
            return this;
        }

        public Builder addOption(OptionMetadata option) {
            this.metadata.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<OptionMetadata> options) {
            this.metadata.options.addAll(options);
            return this;
        }

        /**
         * @return this builder instance.
         */
        public Builder hidden() {
            this.metadata.hidden = true;
            return this;
        }
    }
}
