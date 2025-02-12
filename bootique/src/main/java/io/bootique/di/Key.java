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

import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An object that encapsulates a key used to store and lookup DI bindings. Key is made of
 * a binding type and an optional qualifier (name or annotation).
 */
public class Key<T> {

    private static final KeyQualifier NO_QUALIFIER = new NoQualifier();

    /**
     * Creates a key for a nameless binding of a given type.
     */
    public static <T> Key<T> get(Class<T> type) {
        return get(TypeLiteral.of(type));
    }

    /**
     * Creates a key for a named binding of a given type. 'bindingName' that is an empty
     * String is treated the same way as a null 'bindingName'. In both cases a nameless
     * binding key is created.
     */
    public static <T> Key<T> get(Class<T> type, String bindingName) {
        return get(TypeLiteral.of(type), bindingName);
    }

    /**
     * Creates a key for a qualified by annotation binding of a given type.
     *
     */
    public static <T> Key<T> get(Class<T> type, Class<? extends Annotation> annotationType) {
        return get(TypeLiteral.of(type), annotationType);
    }

    public static <T> Key<T> get(Class<T> type, Annotation annotationInstance) {
        return get(TypeLiteral.of(type), annotationInstance);
    }


    public static <T> Key<T> get(TypeLiteral<T> typeLiteral) {
        return get(typeLiteral, (String)null);
    }

    public static <T> Key<T> get(TypeLiteral<T> typeLiteral, String bindingName) {
        return new Key<>(typeLiteral, bindingName);
    }

    public static <T> Key<T> get(TypeLiteral<T> typeLiteral, Class<? extends Annotation> annotationType) {
        return new Key<>(typeLiteral, annotationType);
    }

    public static <T> Key<T> get(TypeLiteral<T> typeLiteral, Annotation annotationInstance) {
        return new Key<>(typeLiteral, annotationInstance);
    }


    public static <T> Key<List<T>> getListOf(Class<T> type, Class<? extends Annotation> qualifier) {
        return get(TypeLiteral.listOf(type), qualifier);
    }

    public static <T> Key<List<T>> getListOf(Class<T> type, String bindingName) {
        return get(TypeLiteral.listOf(type), bindingName);
    }

    public static <T> Key<List<T>> getListOf(Class<T> type) {
        return get(TypeLiteral.listOf(type));
    }


    public static <T> Key<Set<T>> getSetOf(Class<T> valueType, Class<? extends Annotation> qualifier) {
        return get(TypeLiteral.setOf(valueType), qualifier);
    }

    public static <T> Key<Set<T>> getSetOf(Class<T> valueType, String bindingName) {
        return get(TypeLiteral.setOf(valueType), bindingName);
    }

    public static <T> Key<Set<T>> getSetOf(Class<T> valueType) {
        return get(TypeLiteral.setOf(valueType));
    }


    public static <K, V> Key<Map<K, V>> getMapOf(Class<K> keyType, Class<V> valueType, Class<? extends Annotation> qualifier) {
        return get(TypeLiteral.mapOf(keyType, valueType), qualifier);
    }

    public static <K, V> Key<Map<K, V>> getMapOf(Class<K> keyType, Class<V> valueType, String bindingName) {
        return get(TypeLiteral.mapOf(keyType, valueType), bindingName);
    }

    public static <K, V> Key<Map<K, V>> getMapOf(Class<K> keyType, Class<V> valueType) {
        return get(TypeLiteral.mapOf(keyType, valueType));
    }


    public static <K, V> Key<Map<K, V>> getMapOf(TypeLiteral<K> keyType, TypeLiteral<V> valueType, Class<? extends Annotation> qualifier) {
        return get(TypeLiteral.mapOf(keyType, valueType), qualifier);
    }

    public static <K, V> Key<Map<K, V>> getMapOf(TypeLiteral<K> keyType, TypeLiteral<V> valueType, String bindingName) {
        return get(TypeLiteral.mapOf(keyType, valueType), bindingName);
    }

    public static <K, V> Key<Map<K, V>> getMapOf(TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
        return get(TypeLiteral.mapOf(keyType, valueType));
    }

    public static <T> Key<Optional<T>> getOptionalOf(Class<? extends T> type) {
        return get(TypeLiteral.optionalOf(type));
    }

    /**
     * Creates Optional&lt;T&gt; version of given key
     */
    public static <T> Key<Optional<T>> getOptionalOf(Key<T> key) {
        TypeLiteral<Optional<T>> type = TypeLiteral.optionalOf(key.getType());
        if(key.getBindingName() != null) {
            return get(type, key.getBindingName());
        }
        if(key.getBindingAnnotation() != null) {
            return get(type, key.getBindingAnnotation());
        }
        return get(type);
    }

    private final TypeLiteral<T> type;
    private final KeyQualifier qualifier;

    protected Key(TypeLiteral<T> type, String bindingName) {
        this.type = TypeLiteral.normalize(type);
        // empty non-null binding names are often passed from annotation defaults and are treated as no qualifier
        this.qualifier = bindingName != null && bindingName.length() > 0
                ? new NamedKeyQualifier(bindingName)
                : NO_QUALIFIER;
    }

    protected Key(TypeLiteral<T> type, Class<? extends Annotation> annotationType) {
        this.type = TypeLiteral.normalize(type);
        // null annotation type treated as no qualifier
        this.qualifier = annotationType == null
                ? NO_QUALIFIER
                : new AnnotationTypeQualifier(annotationType);
    }

    protected Key(TypeLiteral<T> type, Annotation annotationInstance) {
        this.type = TypeLiteral.normalize(type);
        if(annotationInstance == null) {
            // null annotation type treated as no qualifier
            this.qualifier = NO_QUALIFIER;
        } else if(annotationInstance instanceof Named) {
            // special case for @Named annotation
            String name = ((Named) annotationInstance).value();
            this.qualifier = !name.isEmpty() ? new NamedKeyQualifier(name) : NO_QUALIFIER;
        } else if(annotationInstance instanceof javax.inject.Named) {
            // special case for @Named annotation
            String name = ((javax.inject.Named) annotationInstance).value();
            this.qualifier = !name.isEmpty() ? new NamedKeyQualifier(name) : NO_QUALIFIER;
        } else {
            // general case
            this.qualifier = new AnnotationTypeQualifier(annotationInstance.annotationType());
        }
    }

    public TypeLiteral<T> getType() {
        return type;
    }

    /**
     * Returns an optional name of the binding used to distinguish multiple bindings of
     * the same object type.
     */
    public String getBindingName() {
        if (qualifier instanceof NamedKeyQualifier) {
            return ((NamedKeyQualifier) qualifier).getName();
        }
        return null;
    }

    public Class<? extends Annotation> getBindingAnnotation() {
        if (qualifier instanceof AnnotationTypeQualifier) {
            return ((AnnotationTypeQualifier) qualifier).getAnnotationType();
        }
        return null;
    }

    @Override
    public boolean equals(Object object) {

        if (object == this) {
            return true;
        }

        if (object instanceof Key<?>) {
            Key<?> key = (Key<?>) object;

            // type is guaranteed to be not null, so skip null checking...
            if (!type.equals(key.type)) {
                return false;
            }

            // compare additional qualifier
            return qualifier.equals(key.qualifier);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 407 + 11 * type.hashCode() + qualifier.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<BindingKey: ");
        buffer.append(type);

        if (qualifier != NO_QUALIFIER) {
            buffer.append(", ").append(qualifier);
        }

        buffer.append('>');
        return buffer.toString();
    }

    /**
     * Marker interface for additional key qualifiers
     */
    interface KeyQualifier {
        // Implementation should define these methods, but we can't enforce it
        @Override
        boolean equals(Object other);

        @Override
        int hashCode();

        @Override
        String toString();
    }

    static final class NoQualifier implements KeyQualifier {
        private NoQualifier() {
        }

        @Override
        public boolean equals(Object other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return "no qualifier";
        }
    }

    static class AnnotationTypeQualifier implements KeyQualifier {

        private final Class<? extends Annotation> annotationType;

        AnnotationTypeQualifier(Class<? extends Annotation> annotationType) {
            this.annotationType = Objects.requireNonNull(annotationType);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof AnnotationTypeQualifier)) {
                return false;
            }

            return ((AnnotationTypeQualifier) other).annotationType.equals(annotationType);
        }

        @Override
        public int hashCode() {
            return annotationType.hashCode();
        }

        @Override
        public String toString() {
            return "@" + annotationType.getName();
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }
    }

    static class NamedKeyQualifier implements KeyQualifier {

        private final String name;

        NamedKeyQualifier(String name) {
            this.name = Objects.requireNonNull(name);
        }

        private String getName() {
            return name;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof NamedKeyQualifier)) {
                return false;
            }

            return ((NamedKeyQualifier) other).getName().equals(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "'" + name + "'";
        }
    }
}
