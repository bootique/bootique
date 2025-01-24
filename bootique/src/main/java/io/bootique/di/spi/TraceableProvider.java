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
import jakarta.inject.Provider;

import java.util.Objects;

/**
 * Provider that wraps other provider to keep trace of injection.
 *
 * @param <T> type of provided object
 */
class TraceableProvider<T> implements Provider<T> {

    private final Key<T> key;
    private final Provider<T> delegate;
    private final DefaultInjector injector;

    TraceableProvider(Key<T> key, Provider<T> delegate, DefaultInjector injector) {
        this.key = Objects.requireNonNull(key);
        this.delegate = Objects.requireNonNull(delegate);
        this.injector = Objects.requireNonNull(injector);
    }

    @Override
    public T get() {
        injector.tracePush(key);
        T result;
        try {
            result = delegate.get();
        } catch (Exception ex) {
            return injector.throwException("Underlying provider (%s) thrown exception", ex, DIUtil.getProviderName(delegate));
        }
        if (result == null && delegate != OptionalBindingBuilder.NULL_PROVIDER) {
            // throw early here, to trace this error with more details
            injector.throwException("Underlying provider (%s) returned NULL instance", DIUtil.getProviderName(delegate));
        }
        injector.tracePop();
        return result;
    }

    @SuppressWarnings("unchecked")
    <P extends Provider<T>> P unwrap() {
        return (P)delegate;
    }

    public Key<T> getKey() {
        return key;
    }
}
