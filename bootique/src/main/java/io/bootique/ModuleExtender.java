/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import java.lang.annotation.Annotation;

/**
 * An optional convenience superclass of Module extenders that defines a typical extender structure.
 *
 * @since 0.22
 */
public abstract class ModuleExtender<T extends ModuleExtender<T>> {

    protected Binder binder;

    public ModuleExtender(Binder binder) {
        this.binder = binder;
    }

    /**
     * Initializes empty DI collections and maps managed by this extender. Should be called from "configure" method of
     * the owning module.
     *
     * @return this extender instance.
     */
    public abstract T initAllExtensions();

    protected <V> Multibinder<V> newSet(Class<V> elementType) {
        return Multibinder.newSetBinder(binder, elementType);
    }

    protected <V> Multibinder<V> newSet(Class<V> elementType, Class<? extends Annotation> annotatedWith) {
        return Multibinder.newSetBinder(binder, elementType, annotatedWith);
    }

    protected <V> Multibinder<V> newSet(Key<V> diKey) {
        return Multibinder.newSetBinder(binder, diKey);
    }

    protected <K, V> MapBinder<K, V> newMap(Class<K> keyType, Class<V> elementType) {
        return MapBinder.newMapBinder(binder, keyType, elementType);
    }

    protected <K, V> MapBinder<K, V> newMap(TypeLiteral<K> keyType, TypeLiteral<V> elementType) {
        return MapBinder.newMapBinder(binder, keyType, elementType);
    }

    protected <K, V> MapBinder<K, V> newMap(Class<K> keyType, Class<V> elementType, Class<? extends Annotation> annotatedWith) {
        return MapBinder.newMapBinder(binder, keyType, elementType, annotatedWith);
    }
}
