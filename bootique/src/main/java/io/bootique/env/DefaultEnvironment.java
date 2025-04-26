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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Environment} implementation that reads properties and variables from the Map passed in constructor.
 */
public class DefaultEnvironment implements Environment {

    static final String FRAMEWORK_PROPERTIES_PREFIX = "bq";

    /**
     * If present, enables boot sequence tracing to STDERR.
     */
    public static final String TRACE_PROPERTY = "bq.trace";

    private final Map<String, String> properties;

    public static Builder builder() {
        return new Builder();
    }

    protected DefaultEnvironment(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Map<String, String> frameworkProperties() {
        return filterByPrefix(properties, FRAMEWORK_PROPERTIES_PREFIX, ".");
    }

    protected static Map<String, String> filterByPrefix(Map<String, String> unfiltered, String prefix, String separator) {
        String lPrefix = prefix.endsWith(separator) ? prefix : prefix + separator;
        int len = lPrefix.length();

        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> e : unfiltered.entrySet()) {
            if (e.getKey().startsWith(lPrefix)) {
                filtered.put(e.getKey().substring(len), e.getValue());
            }
        }

        return filtered;
    }

    public static class Builder {
        private Map<String, String> diProperties;
        private Map<String, String> diVariables;
        private Collection<DeclaredVariable> declaredVariables;
        private boolean excludeSystemProperties;
        private boolean excludeSystemVariables;

        private Builder() {
        }

        public DefaultEnvironment build() {
            return new DefaultEnvironment(buildProperties());
        }

        public Builder excludeSystemProperties() {
            excludeSystemProperties = true;
            return this;
        }

        public Builder excludeSystemVariables() {
            excludeSystemVariables = true;
            return this;
        }

        public Builder diProperties(Map<String, String> diProperties) {
            this.diProperties = diProperties;
            return this;
        }

        public Builder diVariables(Map<String, String> diVariables) {
            this.diVariables = diVariables;
            return this;
        }

        public Builder declaredVariables(Collection<DeclaredVariable> declaredVariables) {
            this.declaredVariables = declaredVariables;
            return this;
        }

        protected Map<String, String> buildProperties() {

            Map<String, String> properties = new HashMap<>();

            // order of config overrides
            // 1. DI properties
            // 2. System properties
            // 3. DI declared vars
            // 4. System declared vars

            if (this.diProperties != null) {
                properties.putAll(this.diProperties);
            }

            if (!excludeSystemProperties) {
                // override DI props from system...
                System.getProperties().forEach((k, v) -> properties.put((String) k, (String) v));
            }

            declaredVariables.forEach(dv -> mergeValue(dv, properties, diVariables));

            if (!excludeSystemVariables) {
                Map<String, String> systemVars = System.getenv();
                declaredVariables.forEach(dv -> mergeValue(dv, properties, systemVars));
            }

            return properties;
        }

        private void mergeValue(DeclaredVariable dv, Map<String, String> properties, Map<String, String> vars) {
            String value = vars.get(dv.getName());
            if (value != null) {
                String canonicalProperty = FRAMEWORK_PROPERTIES_PREFIX + "." + dv.getConfigPath();
                properties.put(canonicalProperty, value);
            }
        }
    }
}
