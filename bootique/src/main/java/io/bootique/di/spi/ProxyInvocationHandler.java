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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class ProxyInvocationHandler<T> implements InvocationHandler {

    private final DefaultInjector injector;
    private final Key<T> key;

    private volatile T instance;

    ProxyInvocationHandler(DefaultInjector injector, Key<T> key) {
        this.injector = injector;
        this.key = key;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
        method.setAccessible(true);
        return method.invoke(getInstance(), args);
    }

    T getInstance() {
        T local = instance;
        if(local == null) {
            synchronized (key) {
                local = instance;
                if(local == null) {
                    local = instance = injector.getInstanceWithCycleProtection(key, true);
                }
            }
        }
        return local;
    }
}
