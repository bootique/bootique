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

package io.bootique.meta.config;

import java.lang.reflect.Type;

/**
 * @since 0.21
 */
class ConfigMetadataNodeProxy implements ConfigMetadataNode {

    private String name;
    private String description;
    private ConfigMetadataNode delegate;

    ConfigMetadataNodeProxy(String name, String description, ConfigMetadataNode delegate) {
        this.name = name;
        this.description = description;
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Type getType() {
        return delegate.getType();
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return delegate.accept(visitor);
    }
}
