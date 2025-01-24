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

import io.bootique.di.TypeLiteral;
import jakarta.inject.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class FieldInjectingDecoratorProvider<T> implements DecoratorProvider<T> {

    private final Class<? extends T> implementation;
    private final DefaultInjector injector;
    private final DecoratorProvider<T> delegate;

    FieldInjectingDecoratorProvider(Class<? extends T> implementation, DecoratorProvider<T> delegate,
            DefaultInjector injector) {
        this.delegate = delegate;
        this.injector = injector;
        this.implementation = implementation;
    }

    @Override
    public Provider<T> get(final Provider<T> undecorated) {
        return new FieldInjectingProvider<T>(delegate.get(undecorated), injector) {

            @Override
            protected Object value(Field field, TypeLiteral<?> fieldType, Annotation bindingAnnotation) {
                // delegate (possibly) injected as Provider
                if (injector.getPredicates().isProviderType(fieldType.getRawType())) {

                    Class<?> objectClass = GenericTypesUtils.parameterClass(field.getGenericType());

                    if (objectClass == null) {
                        return injector.throwException("Provider field %s.%s of type %s must be "
                                + "parameterized to be usable for injection", field.getDeclaringClass().getName(),
                                field.getName(), fieldType.getRawType().getName());
                    }

                    if(objectClass.isAssignableFrom(implementation)) {
                        return undecorated;
                    }
                } else if (fieldType.getRawType().isAssignableFrom(implementation)) {
                    return undecorated.get();
                }

                return super.value(field, fieldType, bindingAnnotation);
            }
        };
    }
}
