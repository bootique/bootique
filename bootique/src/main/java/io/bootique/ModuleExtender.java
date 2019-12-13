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

package io.bootique;


import java.lang.annotation.Annotation;

import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.di.MapBuilder;
import io.bootique.di.SetBuilder;
import io.bootique.di.TypeLiteral;

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

    protected <V> SetBuilder<V> newSet(Class<V> elementType) {
        return binder.bindSet(elementType);
    }

    protected <V> SetBuilder<V> newSet(Class<V> elementType, Class<? extends Annotation> annotatedWith) {
        return binder.bindSet(elementType, annotatedWith);
    }

    protected <V> SetBuilder<V> newSet(Key<V> diKey) {
        if(diKey.getBindingAnnotation() != null) {
            return binder.bindSet(diKey.getType(), diKey.getBindingAnnotation());
        } else if(diKey.getBindingName() != null) {
            return binder.bindSet(diKey.getType(), diKey.getBindingName());
        } else {
            return binder.bindSet(diKey.getType());
        }
    }

    protected <K, V> MapBuilder<K, V> newMap(Class<K> keyType, Class<V> elementType) {
        return binder.bindMap(keyType, elementType);
    }

    protected <K, V> MapBuilder<K, V> newMap(TypeLiteral<K> keyType, TypeLiteral<V> elementType) {
        return binder.bindMap(keyType, elementType);
    }

    protected <K, V> MapBuilder<K, V> newMap(Class<K> keyType, Class<V> elementType, Class<? extends Annotation> annotatedWith) {
        return binder.bindMap(keyType, elementType, annotatedWith);
    }
}
