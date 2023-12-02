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
import io.bootique.di.TypeLiteral;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Predicate;

/**
 * Resolves provider methods to a set of bindings. Provider methods are a part of a module class, each annotated
 * with a specified annotation. Usually this annotations is {@link io.bootique.di.Provides @Provides}.
 */
class ProvidesHandler {

    private final DefaultInjector injector;

    ProvidesHandler(DefaultInjector injector) {
        this.injector = injector;
    }

    void bindingsFromAnnotatedMethods(Object module) {
        Predicate<Method> providesMethodPredicate = injector.getPredicates().getProvidesMethodPredicate();

        // consider annotated methods in the module class
        for (Method m : module.getClass().getDeclaredMethods()) {
            if (providesMethodPredicate.test(m)) {
                validateProvidesMethod(module, m);
                m.setAccessible(true);
                // change to mutable array on first match
                createBinding(module, m);
            }
        }
    }

    private void validateProvidesMethod(Object module, Method method) {
        if (void.class.equals(method.getReturnType())) {
            injector.throwException(
                    "Provider method '%s()' on module '%s' is void. To be a proper provider method, it must return a value",
                    method.getName(), module.getClass().getName());
        }
    }

    private <T> void createBinding(Object module, Method method) {
        Key<T> key = createKey(method.getGenericReturnType(), extractQualifier(method, method.getDeclaredAnnotations()));
        Binding<T> binding = createBinding(key, module, method);

        injector.putBinding(key, binding);
    }

    private Annotation extractQualifier(Method method, Annotation[] annotations) {
        Annotation found = null;
        Predicate<Class<? extends Annotation>> qualifierPredicate = injector.getPredicates().getQualifierPredicate();
        for (Annotation a : annotations) {
            if (qualifierPredicate.test(a.annotationType())) {
                if (found != null) {
                    injector.throwException("Multiple qualifying annotations found for method '%s()' or its parameter on module '%s'"
                            , method.getName()
                            , method.getDeclaringClass().getName());
                }
                found = a;
            }
        }

        return found;
    }

    private <T> Key<T> createKey(Type bindingType, Annotation qualifier) {
        if (isProviderType(bindingType)) {
            // Use provider generic argument as key
            bindingType = ((ParameterizedType) bindingType).getActualTypeArguments()[0];
        }
        return Key.get(TypeLiteral.of(bindingType), qualifier);
    }

    private boolean isProviderType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return injector.getPredicates().isProviderType(parameterizedType.getRawType());
        }
        return false;
    }

    private <T> Binding<T> createBinding(Key<T> key, Object module, Method method) {
        return new Binding<>(key, createProvider(key, module, method), createScope(method), false);
    }

    private <T> Provider<T> createProvider(Key<T> key, Object module, Method method) {
        Provider<?>[] argumentProviders = createArgumentProviders(method);
        Provider<T> provider = new ProvidesMethodProvider<>(injector, argumentProviders, method, module);
        return injector.wrapProvider(key, provider);
    }

    private Scope createScope(Method method) {
        // force singleton for annotated methods
        if (injector.getPredicates().isSingleton(method)) {
            return injector.getSingletonScope();
        }
        // otherwise use injector's default scope
        return injector.getDefaultScope();
    }

    private Provider<?>[] createArgumentProviders(Method method) {

        Type[] params = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        int len = params.length;
        Provider<?>[] providers = new Provider[len];

        for (int i = 0; i < len; i++) {
            Annotation qualifier = extractQualifier(method, paramAnnotations[i]);
            Key<?> key = createKey(params[i], qualifier);

            if (isProviderType(params[i])) {
                // will resolve to provider of provider
                providers[i] = () -> injector.getProvider(key);
            } else {
                // resolve the actual provider lazily
                providers[i] = () -> injector.getInstance(key);
            }
        }

        return providers;
    }

    /**
     * Separate class just for better error reporting.
     * @param <T> provided type
     */
    private static class ProvidesMethodProvider<T> implements NamedProvider<T> {
        private final DefaultInjector injector;
        private final Provider<?>[] argumentProviders;
        private final Method method;
        private final Object module;

        private ProvidesMethodProvider(DefaultInjector injector, Provider<?>[] argumentProviders, Method method, Object module) {
            this.injector = injector;
            this.argumentProviders = argumentProviders;
            this.method = method;
            this.module = module;
        }

        @Override
        public T get() {
            int len = argumentProviders.length;
            Object[] arguments = new Object[len];

            for (int i = 0; i < len; i++) {
                final int idx = i;
                injector.trace(() -> "Get argument " + idx + " for " + getName());
                arguments[i] = argumentProviders[i].get();
            }

            injector.trace(() -> "Invoking " + getName());
            try {
                @SuppressWarnings("unchecked")
                T result = (T) method.invoke(module, arguments);
                return result;
            } catch (Exception e) {
                injector.throwException("Error invoking %s", e, getName());
                return null;
            }
        }

        @Override
        public String getName() {
            return String.format("provider method '%s()' of module '%s'", method.getName(), module.getClass().getName());
        }
    }
}
