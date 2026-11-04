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
 * A flag option without a value.
 */
public class FlagOption implements Option {

    private final OptionMetadata metadata;

    protected FlagOption(OptionMetadata metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
    }

    @Override
    public OptionMetadata getMetadata() {
        return metadata;
    }

    public static class Builder {

        private final OptionMetadata.Builder delegate;

        public Builder(String name) {
            this.delegate = OptionMetadata.builder(name);
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

        public FlagOption build() {
            return new FlagOption(delegate.build());
        }
    }
}
