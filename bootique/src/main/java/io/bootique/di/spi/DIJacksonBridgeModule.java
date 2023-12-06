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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.bootique.annotation.BQConfig;

import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

/**
 * Bridges Bootique DI and Jackson databind framework, supporting various types of object instantiation (via Jackson
 * or via Bootique DI) during JSON-to-object deserialization, as well as Bootique field injection.
 *
 * @since 3.0
 */
class DIJacksonBridgeModule extends SimpleModule {

    private final DefaultInjector injector;
    private final ConcurrentMap<Class<?>, Integer> injectionEnabled;

    public DIJacksonBridgeModule(DefaultInjector injector, Collection<Class<?>> injectionEnabledTypes) {
        this.injector = injector;

        // may be written to concurrently, as it expands lazily
        this.injectionEnabled = new ConcurrentHashMap<>();
        injectionEnabledTypes.forEach(this::enableInjectionInto);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addValueInstantiators(this::createInstantiatorOrDefault);
    }

    /**
     * Ensures the specified type is injectable whether or not it is annotated with {@link BQConfig}. This is usually
     * the case for configuration roots.
     */
    public void enableInjectionInto(Class<?> type) {
        injectionEnabled.putIfAbsent(type, 1);
    }

    private ValueInstantiator createInstantiatorOrDefault(
            DeserializationConfig config,
            BeanDescription beanDesc,
            ValueInstantiator defaultInstantiator) {

        JavaType requestedType = beanDesc.getType();
        Class<?> rawType = requestedType.getRawClass();

        return injectionEnabled.containsKey(rawType) || rawType.getAnnotation(BQConfig.class) != null
                ? bootiqueAwareInstantiator(config, requestedType, defaultInstantiator)
                : defaultInstantiator;
    }

    private ValueInstantiator bootiqueAwareInstantiator(DeserializationConfig config, JavaType type, ValueInstantiator delegate) {
        Constructor c = ConstructorInjectingProvider.findConstructor(type.getRawClass(), injector);
        return c != null
                ? bootiqueFirstInstantiator(config, type, c)
                : jacksonFirstInstantiator(delegate);
    }

    private ValueInstantiator bootiqueFirstInstantiator(DeserializationConfig config, JavaType type, Constructor injectionConstructor) {

        Provider<?> provider0 = new ConstructorInjectingProvider<>(injectionConstructor, injector);
        Provider<?> provider1 = new FieldInjectingProvider<>(provider0, injector);
        Provider<?> provider2 = injector.isMethodInjectionEnabled()
                ? new MethodInjectingProvider<>(provider1, injector) : provider1;

        return new DIJacksonInstantiator(config, type, provider2);
    }

    private ValueInstantiator jacksonFirstInstantiator(ValueInstantiator delegate) {

        // TODO: not particularly efficient, as a chain of providers is created for each object
        UnaryOperator<Object> postInjector = o -> {
            Provider<?> provider0 = new InstanceProvider<>(o);
            Provider<?> provider1 = new FieldInjectingProvider<>(provider0, injector);
            Provider<?> provider2 = injector.isMethodInjectionEnabled()
                    ? new MethodInjectingProvider<>(provider1, injector) : provider1;

            return provider2.get();
        };

        return new DIJacksonDelegateInstantiator(delegate, postInjector);
    }
}
