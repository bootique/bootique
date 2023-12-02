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

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

class ConstructorInjectingDecoratorProvider<T> implements DecoratorProvider<T> {

    private final Class<? extends T> implementation;
    private final DefaultInjector injector;

    ConstructorInjectingDecoratorProvider(Class<? extends T> implementation, DefaultInjector injector) {
        this.implementation = implementation;
        this.injector = injector;
    }

    @Override
    public Provider<T> get(final Provider<T> undecorated) {

        return new ConstructorInjectingProvider<T>(implementation, injector) {
            @Override
            protected Object value(Class<?> parameter, Type genericType, Annotation bindingAnnotation) {

                // delegate (possibly) injected as Provider
                if (injector.getPredicates().isProviderType(parameter)) {

                    Class<?> objectClass = GenericTypesUtils.parameterClass(genericType);

                    if (objectClass == null) {
                        return injector.throwException("Constructor provider parameter %s must be "
                                + "parameterized to be usable for injection", parameter.getName());
                    }

                    if (objectClass.isAssignableFrom(implementation)) {
                        return undecorated;
                    }
                }
                // delegate injected as value
                else if (parameter.isAssignableFrom(implementation)) {
                    return undecorated.get();
                }

                return super.value(parameter, genericType, bindingAnnotation);
            }
        };
    }
}
