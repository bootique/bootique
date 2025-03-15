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

package io.bootique.di;

import jakarta.inject.Provider;

import java.util.Map;

/**
 * A binding builder for map configurations. Creates a parameterized map of type &lt;K, V&gt;.
 *
 * @param <K> A type of the map keys.
 * @param <V> A type of the map values.
 * @since 2.0
 */
public interface MapBuilder<K, V> extends ScopeBuilder {

    MapBuilder<K, V> put(K key, Class<? extends V> interfaceType) throws DIRuntimeException;

    /**
     * @since 2.0
     */
    MapBuilder<K, V> putInstance(K key, V value) throws DIRuntimeException;

    MapBuilder<K, V> put(K key, Key<? extends V> valueKey) throws DIRuntimeException;

    /**
     * @since 2.0
     * @deprecated in favor of {@link #putJakartaProviderInstance(Object, Provider)} 
     */
    @Deprecated(forRemoval = true, since = "3.0")
    MapBuilder<K, V> putProviderInstance(K key, javax.inject.Provider<? extends V> value) throws DIRuntimeException;

    /**
     * @deprecated in favor of {@link #putJakartaProvider(Object, Class)}
     */
    @Deprecated(forRemoval = true, since = "3.0")
    MapBuilder<K, V> putProvider(K key, Class<? extends javax.inject.Provider<? extends V>> value) throws DIRuntimeException;

    /**
     * @since 3.0
     */
    MapBuilder<K, V> putJakartaProviderInstance(K key, Provider<? extends V> value) throws DIRuntimeException;

    /**
     * @since 3.0
     */
    MapBuilder<K, V> putJakartaProvider(K key, Class<? extends Provider<? extends V>> value) throws DIRuntimeException;


    /**
     * @since 2.0
     */
    MapBuilder<K, V> putInstances(Map<K, V> map) throws DIRuntimeException;

    /**
     * @deprecated since 2.0.B1 in favor of {@link #putInstance(Object, Object)} to avoid ambiguity
     */
    @Deprecated
    default MapBuilder<K, V> put(K key, V value) throws DIRuntimeException {
        return putInstance(key, value);
    }

    /**
     * @deprecated since 2.0.B1 in favor of {@link #putProviderInstance(Object, javax.inject.Provider)} to avoid ambiguity
     */
    @Deprecated
    default MapBuilder<K, V> putProvider(K key, javax.inject.Provider<? extends V> value) throws DIRuntimeException {
        return putProviderInstance(key, value);
    }

    /**
     * @deprecated since 2.0.B1 in favor of {@link #putInstances(Map)} to avoid ambiguity
     */
    @Deprecated
    default MapBuilder<K, V> putAll(Map<K, V> map) throws DIRuntimeException {
        return putInstances(map);
    }
}
