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

import io.bootique.di.BQInject;
import io.bootique.di.DIBootstrap;
import io.bootique.di.DIRuntimeException;
import io.bootique.di.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Collection of predicates used internally by injector.
 *
 * @see DIBootstrap#injectorBuilder() methods to customize predicates.
 */
public class InjectorPredicates {

    // Default predicates, based on jakarta.inject
    private Predicate<AccessibleObject> injectPredicate = o ->
            o.isAnnotationPresent(Inject.class) || o.isAnnotationPresent(BQInject.class);

    private Predicate<Method> providesMethodPredicate = m -> m.isAnnotationPresent(Provides.class);

    private Predicate<AnnotatedElement> singletonPredicate = o -> o.isAnnotationPresent(Singleton.class);

    private Predicate<Class<? extends Annotation>> qualifierPredicate = c -> c.isAnnotationPresent(Qualifier.class);

    private Predicate<Type> providerPredicate = Provider.class::equals;

    private Function<Provider<?>, Provider<?>> providerFunction = Function.identity();

    private ExceptionProvider<?> exceptionProvider = DIRuntimeException::new;

    public InjectorPredicates() {
    }

    public void setInjectPredicate(Predicate<AccessibleObject> injectPredicate) {
        this.injectPredicate = injectPredicate;
    }

    public void setProviderPredicate(Predicate<Type> providerPredicate) {
        this.providerPredicate = providerPredicate;
    }

    public void setProvidesMethodPredicate(Predicate<Method> providesMethodPredicate) {
        this.providesMethodPredicate = providesMethodPredicate;
    }

    public void setQualifierPredicate(Predicate<Class<? extends Annotation>> qualifierPredicate) {
        this.qualifierPredicate = qualifierPredicate;
    }

    public void setSingletonPredicate(Predicate<AnnotatedElement> singletonPredicate) {
        this.singletonPredicate = singletonPredicate;
    }

    @SuppressWarnings("unchecked")
    public <T> void setProviderFunction(Function<Provider<T>, Provider<T>> providerFunction) {
        this.providerFunction = (Function) providerFunction;
    }

    public void setExceptionProvider(ExceptionProvider<?> exceptionProvider) {
        this.exceptionProvider = exceptionProvider;
    }

    boolean isSingleton(AnnotatedElement object) {
        return singletonPredicate.test(object);
    }

    boolean hasInjectAnnotation(AccessibleObject object) {
        return injectPredicate.test(object);
    }

    boolean isProviderMethod(Method method) {
        return providesMethodPredicate.test(method);
    }

    boolean isQualifierAnnotation(Annotation annotation) {
        return qualifierPredicate.test(annotation.annotationType());
    }

    boolean isProviderType(Type type) {
        return providerPredicate.test(type);
    }

    @SuppressWarnings("unchecked")
    <T> Provider<T> wrapProvider(Provider<T> provider) {
        return (Provider<T>) providerFunction.apply(provider);
    }

    Predicate<Method> getProvidesMethodPredicate() {
        return providesMethodPredicate;
    }

    Predicate<AccessibleObject> getInjectPredicate() {
        return injectPredicate;
    }

    Predicate<Class<? extends Annotation>> getQualifierPredicate() {
        return qualifierPredicate;
    }

    DIRuntimeException createException(String message, Object... args) {
        return createException(message, null, args);
    }

    DIRuntimeException createException(String message, Throwable cause, Object... args) {
        return exceptionProvider.newException(message, cause, args);
    }

    @FunctionalInterface
    public interface ExceptionProvider<T extends DIRuntimeException> {

        T newException(String message, Throwable cause, Object... args);

    }
}
