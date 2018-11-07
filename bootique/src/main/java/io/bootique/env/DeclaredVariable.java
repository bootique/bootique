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

package io.bootique.env;

import java.util.Objects;

/**
 * An environment variable exposed in the app metadata that binds a value of certain configuration path.
 *
 * @since 0.22
 */
public class DeclaredVariable {

    private String configPath;
    private String name;
    private String description;

    public DeclaredVariable(String configPath, String name, String description) {
        this.configPath = Objects.requireNonNull(configPath);
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getConfigPath() {
        return configPath;
    }

    /**
     * @since 1.0.RC1
     */
    public String getDescription() {
        return description;
    }
}
