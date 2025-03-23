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

import io.bootique.meta.MetadataNode;

/**
 * A descriptor of a command-line option.
 */
public class OptionMetadata implements MetadataNode {

    private String name;
    private String description;
    private String shortName;
    private OptionValueCardinality valueCardinality;
    private String valueName;
    private String defaultValue;

    /**
     * @since 3.0
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    protected OptionMetadata() {
    }

    protected OptionMetadata(
            String name,
            String description,
            String shortName,
            OptionValueCardinality valueCardinality,
            String valueName,
            String defaultValue) {

        this.defaultValue = defaultValue;
        this.description = description;
        this.name = name;
        this.shortName = shortName;
        this.valueCardinality = valueCardinality;
        this.valueName = valueName;
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
     * Returns option short name. It can be null if the short name conflicts with short names of other options (of if
     * the app author decides to make it null).
     */
    public String getShortName() {
        return shortName;
    }

    public OptionValueCardinality getValueCardinality() {
        return valueCardinality;
    }

    public String getValueName() {
        return valueName;
    }

    /**
     * Returns the default value for this option. I.e. the value that will be used if the option is provided on
     * command line without an explicit value.
     *
     * @return the default value for this option.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    public static class Builder {

        private final OptionMetadata option;
        private boolean shortNameSet;

        protected Builder() {
            this.option = new OptionMetadata();
            this.option.valueCardinality = OptionValueCardinality.NONE;
        }

        public Builder name(String name) {
            this.option.name = validateName(name);
            return this;
        }

        public Builder shortName(String shortName) {
            option.shortName = shortName != null ? validateShortName(shortName) : null;
            shortNameSet = true;
            return this;
        }

        public Builder shortName(char shortName) {
            return shortName(String.valueOf(shortName));
        }

        public Builder description(String description) {
            this.option.description = description;
            return this;
        }

        public Builder valueRequired() {
            return valueRequired("");
        }

        public Builder valueRequired(String valueName) {
            this.option.valueCardinality = OptionValueCardinality.REQUIRED;
            this.option.valueName = valueName;
            return this;
        }

        public Builder valueOptional() {
            return valueOptional("");
        }

        public Builder valueOptional(String valueName) {
            this.option.valueCardinality = OptionValueCardinality.OPTIONAL;
            this.option.valueName = valueName;
            return this;
        }

        /**
         * Marks value optional and sets the default value for this option that will be used if the option is provided on
         * command line without an explicit value.
         *
         * @param defaultValue a default value for the option.
         * @return this builder instance
         */
        public Builder valueOptionalWithDefault(String defaultValue) {
            return valueOptionalWithDefault("", defaultValue);
        }

        /**
         * Marks value optional and sets the default value for this option that will be used if the option is provided on
         * command line without an explicit value.
         *
         * @param valueName    a description of value
         * @param defaultValue a default value for the option.
         * @return this builder instance
         */
        public Builder valueOptionalWithDefault(String valueName, String defaultValue) {
            this.option.defaultValue = defaultValue;
            return valueOptional(valueName);
        }

        public OptionMetadata build() {
            validateName(option.name);
            if (!shortNameSet) {
                option.shortName = option.name.substring(0, 1);
            }

            return option;
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

        private String validateShortName(String shortName) {
            if (shortName.length() != 1) {
                throw new IllegalArgumentException("'shortName' must be exactly one char long: " + shortName);
            }

            return shortName;
        }
    }

}
