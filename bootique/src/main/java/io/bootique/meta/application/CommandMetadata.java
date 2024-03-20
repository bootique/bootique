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
import java.util.function.Consumer;

public class CommandMetadata implements MetadataNode {

    private String name;
    private String description;
    private String shortName;
    private boolean hidden;
    private final Collection<OptionMetadata> options;
    private OptionValueCardinality valueCardinality;
    private String valueName;
    private String defaultValue;

    public CommandMetadata() {
        this.options = new ArrayList<>();
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
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns an option representation of this command, that may be used in help generation or exposing the command
     * in a CLI parser.
     *
     * @return option representation of this command.
     */
    public OptionMetadata asOption() {
        OptionValueCardinality cardinality = getValueCardinality();
        String valueName = getValueName();

        Consumer<OptionMetadata.Builder> optionMetadataConsumer = createOptionsConsumer(cardinality, valueName);

        // TODO: cache the value?
        // using getters instead of vars; some getters have logic
        return OptionMetadata.builder(getName())
                .shortName(getShortName())
                .description(getDescription())
                .applyCardinality(optionMetadataConsumer)
                .build();
    }

    private static Consumer<OptionMetadata.Builder> createOptionsConsumer(OptionValueCardinality cardinality, String valueName) {
        Consumer<OptionMetadata.Builder> optionsConsumer = builder -> {
            switch (cardinality) {
                case REQUIRED:
                    builder.valueRequired(valueName);
                    break;
                case OPTIONAL:
                    builder.valueOptional(valueName);
                    break;
                case NONE:
                    break;
                default:
                    throw new IllegalStateException("Unknown command value cardinality: " + cardinality);
            }
        };

        return optionsConsumer;
    }

    public Collection<OptionMetadata> getOptions() {
        return options;
    }

    /**
     * Returns the short name
     *
     * @return command short name.
     */
    public String getShortName() {
        return (shortName != null) ? shortName : name.substring(0, 1);
    }

    public OptionValueCardinality getValueCardinality() {
        return valueCardinality;
    }

    public String getValueName() {
        return valueName;
    }

    /**
     * Returns the default value for this command. I.e. the value that will be used if the command is provided on
     * command line without an explicit value.
     *
     * @return the default value for this command.
     */
    public String getDefaultValue() {
        return defaultValue;
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

        private Builder() {
            this.metadata = new CommandMetadata();
            this.metadata.valueCardinality = OptionValueCardinality.NONE;
        }

        public CommandMetadata build() {
            validateName(metadata.name);
            return metadata;
        }

        public Builder commandType(Class<? extends Command> commandType) {
            metadata.name = NAME_BUILDER.toName(commandType);
            return this;
        }

        public Builder name(String name) {
            metadata.name = validateName(name);
            return this;
        }

        public Builder shortName(char shortName) {
            metadata.shortName = String.valueOf(shortName);
            return this;
        }

        public Builder description(String description) {
            this.metadata.description = description;
            return this;
        }

        public CommandMetadata.Builder valueRequired() {
            return valueRequired("");
        }

        public CommandMetadata.Builder valueRequired(String valueName) {
            this.metadata.valueCardinality = OptionValueCardinality.REQUIRED;
            this.metadata.valueName = valueName;
            return this;
        }

        public CommandMetadata.Builder valueOptional() {
            return valueOptional("");
        }

        public CommandMetadata.Builder valueOptional(String valueName) {
            this.metadata.valueCardinality = OptionValueCardinality.OPTIONAL;
            this.metadata.valueName = valueName;
            return this;
        }

        /**
         * Marks value optional and sets the default value for this command that will be used if the command is provided on
         * command line without an explicit value.
         *
         * @param defaultValue a default value for the command.
         * @return this builder instance
         */
        public CommandMetadata.Builder valueOptionalWithDefault(String defaultValue) {
            return valueOptionalWithDefault("", defaultValue);
        }

        /**
         * Marks value optional and sets the default value for this command that will be used if the command is provided on
         * command line without an explicit value.
         *
         * @param valueName a description of value
         * @param defaultValue a default value for the option.
         * @return this builder instance
         */
        public CommandMetadata.Builder valueOptionalWithDefault(String valueName, String defaultValue) {
            this.metadata.defaultValue = defaultValue;
            return valueOptional(valueName);
        }

        public Builder addOption(OptionMetadata option) {
            this.metadata.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<OptionMetadata> options) {
            this.metadata.options.addAll(options);
            return this;
        }

        public Builder addOption(OptionMetadata.Builder optionBuilder) {
            return addOption(optionBuilder.build());
        }

        /**
         * @return this builder instance.
         */
        public Builder hidden() {
            this.metadata.hidden = true;
            return this;
        }

        private String validateName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Null 'name'");
            }

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Empty 'name'");
            }

            return name;
        }
    }
}
