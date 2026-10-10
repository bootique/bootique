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

package io.bootique.option;

import io.bootique.meta.application.OptionMetadata;

import java.util.Objects;

/**
 * An option that binds its value to a configuration path.
 */
public class ConfigValueOption extends ValueOption {

    private final String configPath;

    protected ConfigValueOption(OptionMetadata metadata, String configPath) {
        super(metadata);
        this.configPath = Objects.requireNonNull(configPath, "configPath is null");
    }

    public String getConfigPath() {
        return configPath;
    }

    public static class Builder {

        private final String configPath;
        private final OptionMetadata.Builder delegate;

        public Builder(String name, String configPath) {
            this.configPath = Objects.requireNonNull(configPath, "configPath is null");
            this.delegate = OptionMetadata.builder(name);
            this.delegate.valueRequired();
        }

        public Builder description(String description) {
            delegate.description(description);
            return this;
        }

        public Builder shortName(char shortName) {
            delegate.shortName(shortName);
            return this;
        }

        public Builder shortName(String shortName) {
            delegate.shortName(shortName);
            return this;
        }

        public Builder valueRequired() {
            delegate.valueRequired();
            return this;
        }

        public Builder valueRequired(String valueName) {
            delegate.valueRequired(valueName);
            return this;
        }

        public Builder valueOptional() {
            delegate.valueOptional();
            return this;
        }

        public Builder valueOptional(String valueName) {
            delegate.valueOptional(valueName);
            return this;
        }

        public Builder valueOptionalWithDefault(String defaultValue) {
            delegate.valueOptionalWithDefault(defaultValue);
            return this;
        }

        public Builder valueOptionalWithDefault(String valueName, String defaultValue) {
            delegate.valueOptionalWithDefault(valueName, defaultValue);
            return this;
        }

        public ConfigValueOption build() {
            return new ConfigValueOption(delegate.build(), configPath);
        }
    }
}
