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

import java.util.Map;

/**
 * Provides access to runtime properties, all and Bootique-specific. Bootique-specific properties are those that start
 * with "bq.". By default, Environment properties are the same as JVM system properties.
 */
public interface Environment {

    /**
     * @deprecated unused
     */
    @Deprecated(since = "3.0", forRemoval = true)
    String FRAMEWORK_PROPERTIES_PREFIX = "bq";

    /**
     * Return a value for the named property or null if not present.
     */
    String getProperty(String name);

    /**
     * Returns all properties in this Environment.
     *
     * @since 3.0
     */
    Map<String, String> properties();

    /**
     * Returns all properties in this environment that start with a given prefix plus a dot separator. The prefix is
     * stripped from the property name in the Map.
     *
     * @param prefix a prefix to qualify properties with.
     * @return all properties in this environment that start with a given prefix
     * plus a dot separator.
     * @deprecated unused. To retrieve Bootique-specific properties use {@link #frameworkProperties()}
     */
    @Deprecated(since = "3.0", forRemoval = true)
    Map<String, String> subproperties(String prefix);

    /**
     * Returns a subset of properties in this Environment that start with "bq." prefix. The prefix is stripped from the
     * properties in the returned map.
     */
    Map<String, String> frameworkProperties();
}
