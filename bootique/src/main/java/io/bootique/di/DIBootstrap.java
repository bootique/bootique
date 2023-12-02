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

package io.bootique.di;

import io.bootique.BQModule;
import io.bootique.di.spi.DefaultInjector;
import io.bootique.di.spi.InjectorPredicates;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A class that bootstraps the Bootique DI container.
 */
public class DIBootstrap {

    /**
     * Creates injector builder.
     * @return builder
     */
    public static InjectorBuilder injectorBuilder() {
        return new InjectorBuilder();
    }

    /**
     * Creates injector builder.
     * @return builder
     */
    public static InjectorBuilder injectorBuilder(BQModule... modules) {
        return new InjectorBuilder(modules);
    }

    /**
     * Creates injector builder.
     * @return builder
     */
    public static InjectorBuilder injectorBuilder(Collection<BQModule> modules) {
        return injectorBuilder(modules.toArray(new BQModule[0]));
    }

    /**
     * Creates and returns an injector instance working with the set of provided modules.
     * Shortcut for injectorBuilder(modules).build()
     * @return injector with default configuration
     */
    public static Injector createInjector(BQModule... modules) throws DIRuntimeException {
        return injectorBuilder(modules).build();
    }

    /**
     * Creates and returns an injector instance working with the set of provided modules.
     * Shortcut for injectorBuilder(modules).build()
     * @return injector with default configuration
     */
    public static Injector createInjector(Collection<BQModule> modules) {
        return injectorBuilder(modules).build();
    }

    /**
     * Injector builder that allows to configure injector
     */
    public static class InjectorBuilder {
        private Set<DefaultInjector.Options> options;
        private InjectorPredicates injectorPredicates;
        private BQModule[] modules;

        private InjectorBuilder(BQModule... modules) {
            this.options = EnumSet.noneOf(DefaultInjector.Options.class);
            this.modules = modules;
            this.injectorPredicates = new InjectorPredicates();
        }

        /**
         * Disable dynamic (i.e. not registered directly in binder) binding resolution.
         * If disabled, Injector will throw in case of the unknown binding.
         * Enabled by default.
         *
         * @return this
         */
        public InjectorBuilder disableDynamicBindings() {
            this.options.add(DefaultInjector.Options.DISABLE_DYNAMIC_BINDINGS);
            return this;
        }

        /**
         * Allow only declared overrides.
         * Disabled by default, all overrides allowed.
         *
         * @return this
         */
        public InjectorBuilder declaredOverridesOnly() {
            this.options.add(DefaultInjector.Options.DECLARED_OVERRIDE_ONLY);
            return this;
        }

        /**
         * Use singleton scope for bindings by default, otherwise no scope will be used.
         *
         * @return this
         */
        public InjectorBuilder defaultSingletonScope() {
            this.options.add(DefaultInjector.Options.SINGLETON_SCOPE_BY_DEFAULT);
            return this;
        }

        /**
         * Enable injection into methods.
         * Disabled by default.
         *
         * @return this
         */
        public InjectorBuilder enableMethodInjection() {
            this.options.add(DefaultInjector.Options.ENABLE_METHOD_INJECTION);
            return this;
        }

        /**
         *
         * Disable detailed injection trace (e.g. in production environment).
         * Enabled by default.
         *
         * @return this
         */
        public InjectorBuilder disableTrace() {
            this.options.add(DefaultInjector.Options.DISABLE_TRACE);
            return this;
        }

        /**
         * Disable auto-proxy creation for simple circular dependencies resolution
         *
         * @return this
         */
        public InjectorBuilder disableProxyCreation() {
            options.add(DefaultInjector.Options.DISABLE_PROXY);
            return this;
        }

        /**
         * Set custom predicate for methods in modules that should be used as providers.
         * Default predicate test methods for {@link Provides} annotation.
         *
         * @param providesMethodPredicate method predicate
         * @return this
         */
        public InjectorBuilder withProvidesMethodPredicate(Predicate<Method> providesMethodPredicate) {
            injectorPredicates.setProvidesMethodPredicate(providesMethodPredicate);
            return this;
        }

        /**
         * Set custom inject predicate.
         * Default predicate test constructors, methods and fields for {@link javax.inject.Inject} annotation.
         *
         * @param injectPredicate inject predicate
         * @return this
         */
        public InjectorBuilder withInjectAnnotationPredicate(Predicate<AccessibleObject> injectPredicate) {
            injectorPredicates.setInjectPredicate(injectPredicate);
            return this;
        }

        /**
         * Set custom predicate for Provider type.
         * By default {@link Provider} class is used.
         *
         * @param providerPredicate provider type predicate
         * @return this
         */
        public InjectorBuilder withProviderPredicate(Predicate<Type> providerPredicate) {
            injectorPredicates.setProviderPredicate(providerPredicate);
            return this;
        }

        /**
         * Set custom predicate for qualifying annotations.
         * By default tests for {@link javax.inject.Qualifier} annotation.
         *
         * @param qualifierPredicate qualifier predicate
         * @return this
         */
        public InjectorBuilder withQualifierPredicate(Predicate<Class<? extends Annotation>> qualifierPredicate) {
            injectorPredicates.setQualifierPredicate(qualifierPredicate);
            return this;
        }

        /**
         * Set custom singleton scope predicate.
         * By default tests for {@link javax.inject.Singleton} annotation.
         *
         * @param singletonPredicate singleton predicate
         * @return this
         */
        public InjectorBuilder withSingletonPredicate(Predicate<AnnotatedElement> singletonPredicate) {
            injectorPredicates.setSingletonPredicate(singletonPredicate);
            return this;
        }

        /**
         * Set custom provider implementation.
         * By default {@link Provider} used as is.
         *
         * @param providerFunction provider wrapping function
         * @return this
         */
        public <T> InjectorBuilder withProviderWrapper(Function<Provider<T>, Provider<T>> providerFunction) {
            injectorPredicates.setProviderFunction(providerFunction);
            return this;
        }

        /**
         * Set custom exception provider.
         * By default {@link DIRuntimeException#DIRuntimeException(String, Throwable, Object...)} is used.
         *
         * @param provider exception provider
         * @return this
         */
        public InjectorBuilder withExceptionProvider(InjectorPredicates.ExceptionProvider<?> provider) {
            injectorPredicates.setExceptionProvider(provider);
            return this;
        }

        /**
         * Build injector with provided options.
         *
         * @return injector
         */
        public Injector build() {
            return new DefaultInjector(options, injectorPredicates, modules);
        }
    }

}
