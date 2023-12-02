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
import io.bootique.di.Scope;
import io.bootique.di.ScopeBuilder;

import javax.inject.Provider;

/**
 * A superclass of DI List and Map builders.
 *
 * @param <K> DI key type.
 * @param <E> Collection element type.
 */
public abstract class DICollectionBuilder<K, E> implements ScopeBuilder {

    protected final DefaultInjector injector;
    protected final Key<K> bindingKey;

    public DICollectionBuilder(Key<K> bindingKey, DefaultInjector injector) {
        this.injector = injector;
        this.bindingKey = bindingKey;
    }

    protected Provider<E> createInstanceProvider(E value) {
        Provider<E> provider0 = new InstanceProvider<>(value);
        Provider<E> provider1 = new FieldInjectingProvider<>(provider0, injector);
        if(!injector.isMethodInjectionEnabled()) {
            return provider1;
        }
        return new MethodInjectingProvider<>(provider1, injector);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Provider<E> createProviderProvider(Class<? extends Provider<? extends E>> providerType) {
        Key<? extends Provider<? extends E>> providerKey = Key.get(providerType);
        Provider<Provider<? extends E>> providerProvider = () -> {
            injector.trace(() -> "Resolving custom provider of type " + providerType);
            if(!injector.hasProvider(providerKey)) {
                // create new provider
                Provider<Provider<? extends E>> provider0 = new ConstructorInjectingProvider<>(providerType, injector);
                Provider<Provider<? extends E>> provider1 = new FieldInjectingProvider<>(provider0, injector);
                if(injector.isMethodInjectionEnabled()) {
                    provider1 = new MethodInjectingProvider<>(provider1, injector);
                }
                injector.putBinding((Key)providerKey, provider1);
            }
            // get existing provider
            return injector.getInstance(providerKey);
        };

        Provider<E> provider3 = new CustomProvidersProvider<>(injector, providerType, providerProvider);
        Provider<E> provider4 = new FieldInjectingProvider<>(provider3, injector);
        if(injector.isMethodInjectionEnabled()) {
            provider4 = new MethodInjectingProvider<>(provider4, injector);
        }
        return provider4;
    }

    protected <SubT extends E> Provider<SubT> getByTypeProvider(Class<SubT> interfaceType) {
        return getByKeyProvider(Key.get(interfaceType));
    }

    protected <SubT extends E> Provider<SubT> getByKeyProvider(Key<SubT> key) {
        // Create a deferred provider to prevent caching the intermediate provider from the Injector.
        // The actual provider may get overridden after list builder is created.
        if(!injector.hasProvider(key)) {
            injector.putBinding(key, (Provider<SubT>) null);
        }
        return () -> injector.getInstance(key);
    }

    @Override
    public void in(Scope scope) {
        injector.changeBindingScope(bindingKey, scope);
    }

    @Override
    public void inSingletonScope() {
        in(injector.getSingletonScope());
    }

    @Override
    public void withoutScope() {
        in(injector.getNoScope());
    }

    @Override
    public void initOnStartup() {
        injector.markForEarlySetup(bindingKey);
    }
}
