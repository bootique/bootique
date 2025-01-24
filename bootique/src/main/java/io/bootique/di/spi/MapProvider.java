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

import jakarta.inject.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

class MapProvider<K, V> implements Provider<Map<K, V>> {

    private final Map<K, Provider<? extends V>> providers;
    private final DefaultInjector injector;

    MapProvider(DefaultInjector injector) {
        this.providers = new ConcurrentHashMap<>();
        this.injector = injector;
    }

    @Override
    public Map<K, V> get() {
        Map<K, V> map = new HashMap<>();

        for (Entry<K, Provider<? extends V>> entry : providers.entrySet()) {
            injector.trace(() -> "Resolve map key '" + entry.getKey() + "'");
            map.put(entry.getKey(), entry.getValue().get());
        }

        return map;
    }

    void put(K key, Provider<? extends V> provider) {
        providers.put(key, provider);
    }
}
