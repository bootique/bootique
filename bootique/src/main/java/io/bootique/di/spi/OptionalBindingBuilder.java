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

import javax.inject.Provider;
import java.util.Optional;

class OptionalBindingBuilder<T> extends DefaultBindingBuilder<T> {

    static final Provider<?> NULL_PROVIDER = () -> null;

    OptionalBindingBuilder(Key<T> bindingKey, DefaultInjector injector) {
        super(bindingKey, injector);
    }

    @Override
    protected void initBinding() {
        Binding<T> binding = injector.getBinding(bindingKey);
        // do not override existing binding with optional one
        if(binding == null) {
            injector.putOptionalBinding(bindingKey, nullProvider());
        }
        // add binding to Optional<T> type
        injector.putBinding(Key.getOptionalOf(bindingKey), () -> {
            T value = injector.getProvider(bindingKey).get();
            return value == null ? Optional.empty() : Optional.of(value);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Provider<T> nullProvider() {
        return (Provider<T>)NULL_PROVIDER;
    }
}
