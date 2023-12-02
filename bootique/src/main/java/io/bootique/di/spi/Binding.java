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

import javax.inject.Provider;
import java.util.List;

/**
 * A binding encapsulates DI provider scoping settings and allows to change them as many
 * times as needed.
 */
class Binding<T> {

    private final Key<T> key;
    private final Provider<T> original;

    private Provider<T> decorated;
    private Provider<T> scoped;
    private Scope scope;
    private boolean optional;

    Binding(Key<T> key, Provider<T> provider, Scope initialScope, boolean optional) {
        this.key = key;
        this.original = provider;
        this.decorated = provider;
        this.optional = optional;

        changeScope(initialScope);
    }

    void changeScope(Scope scope) {
        if (scope == null) {
            scope = NoScope.INSTANCE;
        } else if(optional) {
            // optional binding should not have scope, as it resolves to null
            scope = NoScope.INSTANCE;
        }

        // TODO: what happens to the old scoped value? Seems like this leaks
        // scope event listeners and may cause unexpected events...

        this.scoped = scope.scope(original);
        this.scope = scope;
    }

    void decorate(DefaultInjector injector, Decoration<T> decoration) {

        List<DecoratorProvider<T>> decorators = decoration.decorators();
        if (decorators.isEmpty()) {
            return;
        }

        Provider<T> provider = this.original;
        for (DecoratorProvider<T> decoratorProvider : decorators) {
            provider = decoratorProvider.get(provider);
        }

        this.decorated = injector.wrapProvider(key, provider);

        // TODO: what happens to the old scoped value? Seems like this leaks
        // scope event listeners and may cause unexpected events...

        this.scoped = scope.scope(decorated);
    }

    Provider<T> getOriginal() {
        return original;
    }

    Provider<T> getScoped() {
        return scoped;
    }

    Scope getScope() {
        return scope;
    }

    void setOptional(boolean optional) {
        this.optional = optional;
    }

    boolean isOptional() {
        return optional;
    }

    Key<T> getKey() {
        return key;
    }
}
