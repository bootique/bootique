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

import io.bootique.di.Key;
import io.bootique.di.SetBuilder;
import jakarta.inject.Provider;

import java.util.Collection;
import java.util.Set;

class DefaultSetBuilder<T> extends DICollectionBuilder<Set<T>, T> implements SetBuilder<T> {

    DefaultSetBuilder(Key<Set<T>> bindingKey, DefaultInjector injector) {
        super(bindingKey, injector);
        findOrCreateSetProvider();
    }

    @Override
    public SetBuilder<T> add(Class<? extends T> interfaceType) {
        findOrCreateSetProvider().add(getByTypeProvider(interfaceType));
        return this;
    }

    @Override
    public SetBuilder<T> addInstance(T value) {
        findOrCreateSetProvider().add(createInstanceProvider(value));
        return this;
    }

    @Override
    public SetBuilder<T> add(Key<? extends T> valueKey) {
        findOrCreateSetProvider().add(getByKeyProvider(valueKey));
        return this;
    }

    @Override
    public SetBuilder<T> addJakartaProviderInstance(Provider<? extends T> provider) {
        findOrCreateSetProvider().add(provider);
        return this;
    }

    @Override
    public SetBuilder<T> addJakartaProvider(Class<? extends Provider<? extends T>> providerType)  {
        findOrCreateSetProvider().add(createProviderProvider(providerType));
        return this;
    }

    @Override
    public SetBuilder<T> addInstances(Collection<T> values) {
        SetProvider<T> provider = findOrCreateSetProvider();
        for (T object : values) {
            provider.add(createInstanceProvider(object));
        }
        return this;
    }

    private SetProvider<T> findOrCreateSetProvider() {

        SetProvider<T> provider;
        Binding<Set<T>> binding = injector.getBinding(bindingKey);
        if (binding == null) {
            provider = new SetProvider<>(injector, bindingKey);
            injector.putBinding(bindingKey, provider);
        } else {
            if (injector.isInjectionTraceEnabled()) {
                provider = ((TraceableProvider<Set<T>>) binding.getOriginal()).unwrap();
            } else {
                provider = (SetProvider<T>) binding.getOriginal();
            }
        }

        return provider;
    }
}
