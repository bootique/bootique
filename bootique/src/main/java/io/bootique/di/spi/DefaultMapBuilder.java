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

package io.bootique.di.spi;

import io.bootique.di.DIRuntimeException;
import io.bootique.di.Key;
import io.bootique.di.MapBuilder;
import jakarta.inject.Provider;

import java.util.Map;
import java.util.Map.Entry;

// TODO: current implementation does nothing in case of overriding the key
class DefaultMapBuilder<K, V> extends DICollectionBuilder<Map<K, V>, V> implements MapBuilder<K, V> {

    DefaultMapBuilder(Key<Map<K, V>> bindingKey, DefaultInjector injector) {
        super(bindingKey, injector);

        // trigger initialization of the MapProvider right away, as we need to bind an
        // empty map even if the user never calls 'put'
        findOrCreateMapProvider();
    }

    @Override
    public MapBuilder<K, V> put(K key, Class<? extends V> interfaceType) {
        Provider<? extends V> provider = getByTypeProvider(interfaceType);
        findOrCreateMapProvider().put(key, provider);
        return this;
    }

    @Override
    public MapBuilder<K, V> putInstance(K key, V value) {
        findOrCreateMapProvider().put(key, createInstanceProvider(value));
        return this;
    }

    @Override
    public MapBuilder<K, V> put(K key, Key<? extends V> valueKey) {
        findOrCreateMapProvider().put(key, getByKeyProvider(valueKey));
        return this;
    }

    @Override
    public MapBuilder<K, V> putProviderInstance(K key, javax.inject.Provider<? extends V> value) throws DIRuntimeException {
        return putJakartaProviderInstance(key, value::get);
    }

    @Override
    public MapBuilder<K, V> putProvider(K key, Class<? extends javax.inject.Provider<? extends V>> value) throws DIRuntimeException {
        findOrCreateMapProvider().put(key, createJavaxProviderProvider(value));
        return this;
    }

    @Override
    public MapBuilder<K, V> putJakartaProviderInstance(K key, Provider<? extends V> value) throws DIRuntimeException {
        findOrCreateMapProvider().put(key, value);
        return this;
    }

    @Override
    public MapBuilder<K, V> putJakartaProvider(K key, Class<? extends Provider<? extends V>> value) throws DIRuntimeException {
        findOrCreateMapProvider().put(key, createProviderProvider(value));
        return this;
    }

    @Override
    public MapBuilder<K, V> putInstances(Map<K, V> map) {

        MapProvider<K, V> provider = findOrCreateMapProvider();

        for (Entry<K, V> entry : map.entrySet()) {
            provider.put(entry.getKey(), createInstanceProvider(entry.getValue()));
        }

        return this;
    }

    private MapProvider<K, V> findOrCreateMapProvider() {
        MapProvider<K, V> provider;

        Binding<Map<K, V>> binding = injector.getBinding(bindingKey);
        if (binding == null) {
            provider = new MapProvider<>(injector);
            injector.putBinding(bindingKey, provider);
        } else {
            if (injector.isInjectionTraceEnabled()) {
                provider = ((TraceableProvider<Map<K, V>>) binding.getOriginal()).unwrap();
            } else {
                provider = (MapProvider<K, V>) binding.getOriginal();
            }
        }

        return provider;
    }
}
