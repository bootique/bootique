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

import java.lang.reflect.*;
import java.util.*;

/**
 * This class represents any generic type T, as there is no support for this in Java.
 * <p>
 * Usage: <pre>
 *     TypeLiteralg&lt;List&lt;Integer&gt;&gt; type = new TypeLiteral&lt;List&lt;Integer&gt;&gt;(){};
 * </pre>
 */
public class TypeLiteral<T> {

    private static final Class<?> WILDCARD_MARKER = WildcardMarker.class;

    private final Class<? super T> type;
    private final String typeName;
    private final String[] argumentTypes;

    public static <T> TypeLiteral<T> of(Class<T> type) {
        return new TypeLiteral<>(type);
    }

    public static <T> TypeLiteral<T> of(Type type) {
        return new TypeLiteral<>(type);
    }

    public static <T> TypeLiteral<T> of(Class<T> rawType, Type... parameters) {
        return new TypeLiteral<>(rawType, parameters);
    }

    /**
     * Creates TypeLiteral that represents List&lt;T&gt; type.
     */
    public static <T> TypeLiteral<List<T>> listOf(Class<? extends T> type) {
        return new TypeLiteral<>(List.class, type);
    }

    /**
     * Creates TypeLiteral that represents List&lt;T&gt; type.
     */
    public static <T> TypeLiteral<List<T>> listOf(TypeLiteral<? extends T> type) {
        return new TypeLiteral<>(List.class, type.toString());
    }

    /**
     * Creates TypeLiteral that represents Set&lt;T&lt; type.
     */
    public static <T> TypeLiteral<Set<T>> setOf(Class<? extends T> valueType) {
        return new TypeLiteral<>(Set.class, valueType);
    }

    /**
     * Creates TypeLiteral that represents Set&lt;T&lt; type.
     */
    public static <T> TypeLiteral<Set<T>> setOf(TypeLiteral<? extends T> valueType) {
        return new TypeLiteral<>(Set.class, valueType.toString());
    }

    /**
     * Creates TypeLiteral that represents Map&lt;K, V&lt; type.
     */
    public static <K, V> TypeLiteral<Map<K, V>> mapOf(Class<? extends K> keyType, Class<? extends V> valueType) {
        return new TypeLiteral<>(Map.class, keyType, valueType);
    }

    /**
     * Creates TypeLiteral that represents Map&lt;K, V&lt; type.
     */
    public static <K, V> TypeLiteral<Map<K, V>> mapOf(TypeLiteral<? extends K> keyType, TypeLiteral<? extends V> valueType) {
        return new TypeLiteral<>(Map.class, keyType.toString(), valueType.toString());
    }

    /**
     * Creates TypeLiteral that represents Optional&lt;T&lt; type.
     */
    public static <T> TypeLiteral<Optional<T>> optionalOf(Class<? extends T> type) {
        return new TypeLiteral<>(Optional.class, type);
    }

    /**
     * Creates TypeLiteral that represents Optional&lt;T&lt; type.
     */
    public static <T> TypeLiteral<Optional<T>> optionalOf(TypeLiteral<? extends T> type) {
        return new TypeLiteral<>(Optional.class, type.toString());
    }

    /**
     * Cuts references to outer objects in case of anonymous subclasses.
     */
    static <T> TypeLiteral<T> normalize(TypeLiteral<T> type) {
        Objects.requireNonNull(type, "Null type");
        if (type.getClass() == TypeLiteral.class) {
            // direct instance, pass through
            return type;
        }
        // just recreate it with same content
        return new TypeLiteral<>(type.getRawType(), type.argumentTypes);
    }

    @SuppressWarnings("unchecked")
    protected TypeLiteral() {
        Type genericType = getGenericSuperclassType(getClass());
        this.type = (Class<T>) getRawType(genericType);
        this.typeName = type.getName();
        Type[] argumentTypes = getArgumentTypes(genericType);
        this.argumentTypes = new String[argumentTypes.length];
        initArgumentTypes(argumentTypes);
    }

    @SuppressWarnings("unchecked")
    private TypeLiteral(Type type) {
        this.type = (Class<T>) getRawType(Objects.requireNonNull(type, "No type"));
        this.typeName = this.type.getName();
        Type[] argumentTypes = getArgumentTypes(type);
        this.argumentTypes = new String[argumentTypes.length];
        initArgumentTypes(argumentTypes);
    }

    private TypeLiteral(Class<? super T> type, String... argumentTypes) {
        this.type = type;
        this.typeName = type.getName();
        this.argumentTypes = argumentTypes;
    }

    private TypeLiteral(Class<? super T> type, Type... argumentsType) {
        this.type = Objects.requireNonNull(type, "No class");
        this.typeName = type.getName();
        this.argumentTypes = new String[argumentsType.length];
        initArgumentTypes(argumentsType);
    }

    private void initArgumentTypes(Type... argumentsType) {
        for (int i = 0; i < argumentsType.length; i++) {
            // recursively resolve argument types..
            this.argumentTypes[i] = new TypeLiteral<>(argumentsType[i]).toString();
        }
    }

    private static Type getGenericSuperclassType(Class<?> subclass) {
        if (subclass.getGenericSuperclass() instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments()[0];
        } else {
            throw new DIRuntimeException("Missing type parameter, use like this: new TypeLiteral<MyType>(){};");
        }
    }

    public Class<? super T> getRawType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof TypeLiteral tl) {
            if (!typeName.equals(tl.typeName)) {
                return false;
            }
            return Arrays.equals(argumentTypes, tl.argumentTypes);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = typeName.hashCode();
        result = 31 * result + Arrays.hashCode(argumentTypes);
        return result;
    }

    @Override
    public String toString() {
        String result = typeName;
        if (argumentTypes.length > 0) {
            result += Arrays.toString(argumentTypes);
        }
        return result;
    }

    private static Type[] getArgumentTypes(Type type) {
        if (type instanceof Class) {
            return new Type[0];
        } else if (type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments();
        } else if (type instanceof GenericArrayType gat) {
            if (gat.getGenericComponentType() instanceof ParameterizedType pt) {
                return pt.getActualTypeArguments();
            }
            throw new IllegalArgumentException("Expected ParameterizedType, got " + gat.getGenericComponentType());
        } else if (type instanceof WildcardType wt) {
            Type[] lowerBounds = wt.getLowerBounds();
            Type[] upperBounds = wt.getUpperBounds();
            Type lower = lowerBounds.length > 0 ? wt.getLowerBounds()[0] : Object.class;
            Type upper = upperBounds.length > 0 ? wt.getUpperBounds()[0] : Object.class;
            return new Type[]{lower, upper};
        } else if (type instanceof TypeVariable) {
            throw new DIRuntimeException("Variable type %s can't be fully resolved", type);
        } else {
            return new Type[]{type};
        }
    }

    private static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType gat) {
            if (gat.getGenericComponentType() instanceof ParameterizedType pt) {
                Class<?> rawType = (Class<?>) pt.getRawType();
                return Array.newInstance(rawType, 0).getClass();
            }
            throw new IllegalArgumentException("Expected ParameterizedType, got " + gat.getGenericComponentType());
        } else if (type instanceof WildcardType) {
            return WILDCARD_MARKER;
        } else {
            return Object.class;
        }
    }

    /**
     * Marker interface for WildcardType
     */
    private interface WildcardMarker {
    }
}
