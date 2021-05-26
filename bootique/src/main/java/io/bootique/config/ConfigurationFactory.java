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

package io.bootique.config;

import io.bootique.type.TypeRef;

/**
 * An injectable object that provides access to a tree of configuration data. The whole configuration tree or its
 * subtrees of can be read as objects of a specified type. More often than not returned configuration objects are
 * themselves "factories" of various services. So ConfigurationFactory can be thought as a "factory of factories".
 */
public interface ConfigurationFactory {

    /**
     * Creates and returns a specified type instance with its state initialized
     * from the configuration tree. "prefix" argument defines sub-configuration
     * location in the tree.
     *
     * @param type   a type of configuration object to create.
     * @param prefix sub-configuration location in the config tree. Pass empty
     *               string to access root config.
     * @param <T>    a type of object given configuration should be deserialized
     *               to.
     * @return a fully initialized object of the specified type.
     */
    <T> T config(Class<T> type, String prefix);

    /**
     * Creates and returns a specified generic type instance with its state
     * initialized from the configuration tree. "prefix" argument defines
     * sub-configuration location in the tree. To make a proper "type"
     * parameter, you would usually create an anonymous inner subclass of
     * TypeRef, with the right generics parameters:
     *
     * <pre>
     * new TypeRef&lt;List&lt;Object&gt;&gt;() {
     * }
     * </pre>
     *
     * @param type   a type of parameterized factory to create. You must create a
     *               subclass of {@link TypeRef} with correct generics parameters.
     * @param prefix sub-configuration location in the config tree. Pass empty
     *               string to access root config.
     * @param <T>    a type of object given configuration should be deserialized to.
     * @return a fully initialized object of the specified type.
     */
    <T> T config(TypeRef<? extends T> type, String prefix);

}
