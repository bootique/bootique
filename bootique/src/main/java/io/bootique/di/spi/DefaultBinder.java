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

import io.bootique.di.*;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

class DefaultBinder implements Binder {

    private DefaultInjector injector;

    DefaultBinder(DefaultInjector injector) {
        this.injector = injector;
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> interfaceType) {
        return bind(Key.get(interfaceType));
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> interfaceType, Class<? extends Annotation> annotationType) {
        return bind(Key.get(interfaceType, annotationType));
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> interfaceType, String bindingName) {
        return bind(Key.get(interfaceType, bindingName));
    }

    @Override
    public <T> BindingBuilder<T> bind(Key<T> key) {
        return new DefaultBindingBuilder<>(key, injector);
    }

    @Override
    public <T> BindingBuilder<T> bindOptional(Class<T> interfaceType) {
        return bindOptional(Key.get(interfaceType));
    }

    @Override
    public <T> BindingBuilder<T> bindOptional(Key<T> key) {
        return new OptionalBindingBuilder<>(key, injector);
    }

    @Override
    public <T> BindingBuilder<T> override(Class<T> interfaceType) {
        return override(Key.get(interfaceType));
    }

    @Override
    public <T> BindingBuilder<T> override(Key<T> key) {
        return new OverrideBindingBuilder<>(key, injector);
    }


    @Override
    public <T> SetBuilder<T> bindSet(Class<T> valueType, Class<? extends Annotation> qualifier) {
        return bindSet(Key.getSetOf(valueType, qualifier));
    }

    @Override
    public <T> SetBuilder<T> bindSet(Class<T> valueType, String bindingName) {
        return bindSet(Key.getSetOf(valueType, bindingName));
    }

    @Override
    public <T> SetBuilder<T> bindSet(Class<T> valueType) {
        return bindSet(Key.getSetOf(valueType));
    }

    @Override
    public <T> SetBuilder<T> bindSet(TypeLiteral<T> valueType, Class<? extends Annotation> qualifier) {
        return bindSet(Key.get(TypeLiteral.setOf(valueType), qualifier));
    }

    @Override
    public <T> SetBuilder<T> bindSet(TypeLiteral<T> valueType, String bindingName) {
        return bindSet(Key.get(TypeLiteral.setOf(valueType), bindingName));
    }

    @Override
    public <T> SetBuilder<T> bindSet(TypeLiteral<T> valueType) {
        return bindSet(Key.get(TypeLiteral.setOf(valueType)));
    }



    @Override
    public <K, V> MapBuilder<K, V> bindMap(Class<K> keyType, Class<V> valueType, Class<? extends Annotation> qualifier) {
        return bindMap(Key.getMapOf(keyType, valueType, qualifier));
    }

    @Override
    public <K, V> MapBuilder<K, V> bindMap(Class<K> keyType, Class<V> valueType, String bindingName) {
        return bindMap(Key.getMapOf(keyType, valueType, bindingName));
    }

    @Override
    public <K, V> MapBuilder<K, V> bindMap(Class<K> keyType, Class<V> valueType) {
        return bindMap(Key.getMapOf(keyType, valueType));
    }

    @Override
    public <K, V> MapBuilder<K, V> bindMap(TypeLiteral<K> keyType, TypeLiteral<V> valueType, Class<? extends Annotation> qualifier) {
        return bindMap(Key.getMapOf(keyType, valueType, qualifier));
    }

    @Override
    public <K, V> MapBuilder<K, V> bindMap(TypeLiteral<K> keyType, TypeLiteral<V> valueType, String bindingName) {
        return bindMap(Key.getMapOf(keyType, valueType, bindingName));
    }

    @Override
    public <K, V> MapBuilder<K, V> bindMap(TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
        return bindMap(Key.getMapOf(keyType, valueType));
    }


    @Override
    public <T> DecoratorBuilder<T> decorate(Class<T> interfaceType) {
        return new DefaultDecoratorBuilder<>(Key.get(interfaceType), injector);
    }

    @Override
    public <T> DecoratorBuilder<T> decorate(Key<T> key) {
        return new DefaultDecoratorBuilder<>(key, injector);
    }


    private <T> SetBuilder<T> bindSet(Key<Set<T>> setKey) {
        return new DefaultSetBuilder<>(setKey, injector);
    }

    private <K, V> MapBuilder<K, V> bindMap(Key<Map<K, V>> mapKey) {
        return new DefaultMapBuilder<>(mapKey, injector);
    }
}
