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

import javax.inject.Provider;

/**
 * A provider that provides scoping for other providers.
 */
public class DefaultScopeProvider<T> implements Provider<T> {

    private final Provider<T> delegate;
    private final DefaultScope scope;

    private volatile T instance;

    public DefaultScopeProvider(DefaultScope scope, Provider<T> delegate) {
        this.scope = scope;
        this.delegate = delegate;

        scope.addScopeEventListener(this);
    }

    @Override
    public T get() {
        T localInstance = instance;
        if (localInstance == null) {
            synchronized (this) {
                localInstance = instance;
                if (localInstance == null) {
                    localInstance = instance = delegate.get();
                    if (localInstance == null) {
                        // TODO: can we use injector.throwException() here?
                        throw new DIRuntimeException("Underlying provider (%s) returned NULL instance"
                                , DIUtil.getProviderName(delegate));
                    }

                    scope.addScopeEventListener(localInstance);
                }
            }
        }

        return localInstance;
    }

    @AfterScopeEnd
    public void afterScopeEnd() throws Exception {
        Object localInstance = instance;

        if (localInstance != null) {
            instance = null;
            scope.removeScopeEventListener(localInstance);
        }
    }
}
