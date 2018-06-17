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

package io.bootique.env;

import java.util.Map;

/**
 * Provides access to system properties and environment variables for the app.
 * Allows to filter properties by prefix to separate Bootique-specific values.
 */
public interface Environment {

    String FRAMEWORK_PROPERTIES_PREFIX = "bq";

    String getProperty(String name);

    /**
     * Returns all properties in this environment that start with a given prefix
     * plus a dot separator. The prefix is stripped from the property name in
     * the Map.
     *
     * @param prefix a prefix to qualify properties with.
     * @return all properties in this environment that start with a given prefix
     * plus a dot separator.
     */
    Map<String, String> subproperties(String prefix);

    /**
     * An equivalent to calling {@link #subproperties(String)} with "bq" prefix
     * argument.
     *
     * @return a map of all properties that start with "bq." prefix.
     */
    default Map<String, String> frameworkProperties() {
        return subproperties(FRAMEWORK_PROPERTIES_PREFIX);
    }
}
