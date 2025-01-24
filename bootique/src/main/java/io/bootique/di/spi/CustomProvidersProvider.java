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

/**
 * A wrapper around a provider that itself generates providers.
 */
class CustomProvidersProvider<T> implements NamedProvider<T> {

    private final DefaultInjector injector;
    private final Class<? extends Provider<? extends T>> providerType;
    private final Provider<Provider<? extends T>> providerOfProviders;

    CustomProvidersProvider(DefaultInjector injector, Class<? extends Provider<? extends T>> providerType, Provider<Provider<? extends T>> providerOfProviders) {
        this.injector = injector;
        this.providerType = providerType;
        this.providerOfProviders = providerOfProviders;
    }

    @Override
    public T get() {
        Provider<? extends T> customProvider = providerOfProviders.get();
        injector.trace(() -> "Invoking " + getName());
        return customProvider.get();
    }

    @Override
    public String getName() {
        return "custom provider of type " + providerType.getName();
    }
}
