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

import java.lang.annotation.Annotation;

/**
 * An object passed to a {@link BQModule} by the DI container during initialization, that
 * provides the API for the module to bind its services to the container. Note that the
 * default {@link Scope} of the bound objects is normally "singleton" and can be changed
 * to "no scope" or a custom scope via a corresponding method of a binding builder. E.g.
 * see {@link BindingBuilder#in(Scope)}.
 */
public interface Binder {

    //----------------------
    //   Simple bindings
    //----------------------

    /**
     * Starts an unnamed binding of a specific interface. Binding should continue using
     * returned BindingBuilder.
     */
    <T> BindingBuilder<T> bind(Class<T> interfaceType);

    /**
     * Starts a binding of a specific interface denoted by specified annotation.
     * Binding should continue using returned BindingBuilder.
     */
    <T> BindingBuilder<T> bind(Class<T> interfaceType, Class<? extends Annotation> annotationType);

    /**
     * Starts a named binding of a specific interface. Binding should continue using
     * returned BindingBuilder.
     */
    <T> BindingBuilder<T> bind(Class<T> interfaceType, String bindingName);

    /**
     * Starts a binding of a specific interface based on a provided binding key. This
     * method is more generic than {@link #bind(Class)} and allows to create named
     * bindings in addition to default ones. Binding should continue using returned
     * BindingBuilder.
     */
    <T> BindingBuilder<T> bind(Key<T> key);

    //----------------------
    //   Optional bindings
    //----------------------

    /**
     * Starts an unnamed optional binding of a specific interface.
     * Binding should continue using returned BindingBuilder.
     */
    <T> BindingBuilder<T> bindOptional(Class<T> interfaceType);

    /**
     * Starts an optional binding of a specific interface based on a provided binding key.
     * This method is more generic than {@link #bind(Class)} and allows to create qualified
     * bindings in addition to default ones.
     * Binding should continue using returned BindingBuilder.
     */
    <T> BindingBuilder<T> bindOptional(Key<T> key);

    //----------------------
    //   Bindings override
    //----------------------

    /**
     *
     * Start override binding of a specific interface.
     *
     * @param interfaceType to override
     * @param <T> binding type
     * @return binding builder to continue override
     */
    <T> BindingBuilder<T> override(Class<T> interfaceType);

    /**
     *
     * Start override binding of a specific interface based on a provided binding key.
     *
     * @param key to override
     * @param <T> binding type
     * @return binding builder to continue override
     */
    <T> BindingBuilder<T> override(Key<T> key);

    //----------------------
    //  Map<K,V> bindings
    //----------------------

    /**
     * Starts a binding of a java.util.Map&lt;K, V&gt; distinguished by its
     * keys type, values type and qualifier annotation.
     * Map binding should continue using returned MapBuilder.
     * This is a type safe way of binding a map.
     */
    <K, V> MapBuilder<K, V> bindMap(Class<K> keyType, Class<V> valueType, Class<? extends Annotation> qualifier);

    /**
     * Starts a binding of a java.util.Map&lt;K, V&gt; distinguished by its
     * keys type, values type and binding name.
     * Map binding should continue using returned MapBuilder.
     * This is a type safe way of binding a map.
     */
    <K, V> MapBuilder<K, V> bindMap(Class<K> keyType, Class<V> valueType, String bindingName);

    /**
     * Starts a binding of a java.util.Map&lt;K, V&gt; distinguished by its keys and values type.
     * Map binding should continue using returned MapBuilder.
     * This is a type safe way of binding a map.
     */
    <K, V> MapBuilder<K, V> bindMap(Class<K> keyType, Class<V> valueType);

    /**
     * Starts a binding of a java.util.Map&lt;K, V&gt; distinguished by its
     * keys type, values type and qualifier annotation.
     * Map binding should continue using returned MapBuilder.
     * This is a type safe way of binding a map.
     */
    <K, V> MapBuilder<K, V> bindMap(TypeLiteral<K> keyType, TypeLiteral<V> valueType, Class<? extends Annotation> qualifier);

    /**
     * Starts a binding of a java.util.Map&lt;K, V&gt; distinguished by its
     * keys type, values type and binding name.
     * Map binding should continue using returned MapBuilder.
     * This is a type safe way of binding a map.
     */
    <K, V> MapBuilder<K, V> bindMap(TypeLiteral<K> keyType, TypeLiteral<V> valueType, String bindingName);

    /**
     * Starts a binding of a java.util.Map&lt;K, V&gt; distinguished by its keys and values type.
     * Map binding should continue using returned MapBuilder.
     * This is a type safe way of binding a map.
     */
    <K, V> MapBuilder<K, V> bindMap(TypeLiteral<K> keyType, TypeLiteral<V> valueType);

    //----------------------
    //   Set<T> bindings
    //----------------------

    /**
     * Starts a binding of a java.util.Set&lt;T&gt; distinguished by its values type and qualifier annotation.
     * Set binding should continue using returned SetBuilder. This is somewhat equivalent of
     * using "bind(Set.class, qualifier)", however returned SetBuilder provides type safety and extra
     * DI capabilities.
     */
    <T> SetBuilder<T> bindSet(Class<T> valueType, Class<? extends Annotation> qualifier);

    /**
     * Starts a binding of a java.util.Set&lt;T&gt; distinguished by its values type and binding name.
     * Set binding should continue using returned SetBuilder. This is somewhat equivalent of
     * using "bind(Set.class, bindingName)", however returned SetBuilder provides type safety and extra
     * DI capabilities.
     */
    <T> SetBuilder<T> bindSet(Class<T> valueType, String bindingName);

    /**
     * Starts a binding of a java.util.Set&lt;T&gt; distinguished by its values type.
     * Set binding should continue using returned SetBuilder.
     * This is somewhat equivalent of using "bind(Set.class, bindingName)",
     * however returned SetBuilder provides type safety and extra DI capabilities.
     */
    <T> SetBuilder<T> bindSet(Class<T> valueType);

    /**
     * Starts a binding of a java.util.Set&lt;T&gt; distinguished by its values type and qualifier annotation.
     * Set binding should continue using returned SetBuilder. This is somewhat equivalent of
     * using "bind(Set.class, qualifier)", however returned SetBuilder provides type safety and extra
     * DI capabilities.
     */
    <T> SetBuilder<T> bindSet(TypeLiteral<T> valueType, Class<? extends Annotation> qualifier);

    /**
     * Starts a binding of a java.util.Set&lt;T&gt; distinguished by its values type and binding name.
     * Set binding should continue using returned SetBuilder. This is somewhat equivalent of
     * using "bind(Set.class, bindingName)", however returned SetBuilder provides type safety and extra
     * DI capabilities.
     */
    <T> SetBuilder<T> bindSet(TypeLiteral<T> valueType, String bindingName);

    /**
     * Starts a binding of a java.util.Set&lt;T&gt; distinguished by its values type.
     * Set binding should continue using returned SetBuilder.
     * This is somewhat equivalent of using "bind(Set.class, bindingName)",
     * however returned SetBuilder provides type safety and extra DI capabilities.
     */
    <T> SetBuilder<T> bindSet(TypeLiteral<T> valueType);

    //----------------------
    //     Decorations
    //----------------------

    /**
     */
    <T> DecoratorBuilder<T> decorate(Class<T> interfaceType);

    /**
     */
    <T> DecoratorBuilder<T> decorate(Key<T> key);
}
