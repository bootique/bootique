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
import io.bootique.di.TypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

class ConstructorInjectingProvider<T> implements NamedProvider<T> {

    private final Constructor<? extends T> constructor;
    private final DefaultInjector injector;
    private final Annotation[] bindingAnnotations;

    ConstructorInjectingProvider(Class<? extends T> implementation, DefaultInjector injector) {
        this.injector = injector;
        this.constructor = findConstructor(implementation);
        this.bindingAnnotations = collectParametersQualifiers(constructor);
    }

    @SuppressWarnings("unchecked")
    private Constructor<? extends T> findConstructor(Class<? extends T> implementation) {

        Constructor<?>[] constructors = implementation.getDeclaredConstructors();
        Constructor<?> lastMatch = null;
        int lastSize = -1;

        // pick the first constructor annotated with @Inject, or the default constructor;
        // constructor with the longest parameter list is preferred if multiple matches are found.
        for (Constructor<?> constructor : constructors) {
            int size = constructor.getParameterCount();
            if (size <= lastSize) {
                continue;
            }

            if (size == 0) {
                lastSize = 0;
                lastMatch = constructor;
                continue;
            }

            if (injector.getPredicates().hasInjectAnnotation(constructor)) {
                lastSize = size;
                lastMatch = constructor;
            }
        }

        if (lastMatch == null) {
            return injector.throwException(
                    "No applicable constructor is found for constructor injection in class '%s'",
                    implementation.getName());
        }

        lastMatch.setAccessible(true);
        return (Constructor<? extends T>) lastMatch;
    }

    private Annotation[] collectParametersQualifiers(Constructor<? extends T> constructor) {
        Annotation[] result = new Annotation[constructor.getParameterCount()];
        Annotation[][] annotations = constructor.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] parameterAnnotations = annotations[i];
            for (Annotation annotation : parameterAnnotations) {
                if(injector.getPredicates().isQualifierAnnotation(annotation)) {
                    result[i] = annotation;
                }
            }
        }
        return result;
    }

    @Override
    public T get() {

        Class<?>[] constructorParameters = constructor.getParameterTypes();
        Type[] genericTypes = constructor.getGenericParameterTypes();
        Object[] args = new Object[constructorParameters.length];

        for (int i = 0; i < constructorParameters.length; i++) {
            final int idx = i;
            injector.trace(() -> "Get argument " + idx + " for " + getName());
            args[i] = value(constructorParameters[i], genericTypes[i], bindingAnnotations[i]);
        }

        try {
            injector.trace(() -> "Invoking " + getName());
            return constructor.newInstance(args);
        } catch (Exception e) {
            return injector.throwException("Error invoking %s", e, getName());
        }
    }

    protected Object value(Class<?> parameter, Type genericType, Annotation bindingAnnotation) {

        if (injector.getPredicates().isProviderType(parameter)) {
            Type parameterType = GenericTypesUtils.getGenericParameterType(genericType);
            if (parameterType == null) {
                return injector.throwException("Constructor provider parameter %s must be "
                        + "parameterized to be usable for injection", parameter.getName());
            }
            return injector.getProvider(Key.get(TypeLiteral.of(parameterType), bindingAnnotation));
        } else {
            Key<?> key = Key.get(TypeLiteral.of(genericType), bindingAnnotation);
            return injector.getInstanceWithCycleProtection(key);
        }
    }

    @Override
    public String getName() {
        return "constructor of class '" + constructor.getDeclaringClass().getName() + "'";
    }
}
