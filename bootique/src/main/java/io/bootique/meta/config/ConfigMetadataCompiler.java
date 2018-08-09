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

package io.bootique.meta.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.help.ValueObjectDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A thread-safe caching compiler of configuration metadata for objects annotated with {@link BQConfig} and
 * {@link BQConfigProperty}.
 *
 * @since 0.21
 */
public class ConfigMetadataCompiler {

    private static final Pattern SETTER = Pattern.compile("^set([A-Z].*)$");

    private Function<Class<?>, Stream<Class<?>>> subclassProvider;
    private Map<Type, ConfigObjectMetadata> seen;
    private Map<Class<?>, ValueObjectDescriptor> descriptorMap;

    /**
     * @deprecated since 0.26 use {@link #ConfigMetadataCompiler(Function, Map)}
     */
    @Deprecated
    public ConfigMetadataCompiler(Function<Class<?>, Stream<Class<?>>> subclassProvider) {
        this.subclassProvider = subclassProvider;
        this.seen = new ConcurrentHashMap<>();
    }

    public ConfigMetadataCompiler(Function<Class<?>, Stream<Class<?>>> subclassProvider, Map<Class<?>, ValueObjectDescriptor> descriptorMap) {
        this.subclassProvider = subclassProvider;
        this.seen = new ConcurrentHashMap<>();
        this.descriptorMap = descriptorMap;
    }

    private static Type propertyTypeFromSetter(Method maybeSetter) {
        Type[] paramTypes = maybeSetter.getGenericParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but does not match expected setter signature."
                    + " It must take exactly one parameter");
        }

        return paramTypes[0];
    }

    private static Type propertyTypeFromConstructor(Constructor maybeConstructor) {
        Type[] paramTypes = maybeConstructor.getGenericParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalStateException("Constructor '" + maybeConstructor.toString()
                    + "' is annotated with @BQConfigProperty, but does not match expected signature."
                    + " It must take exactly one parameter");
        }

        return paramTypes[0];
    }

    private static String propertyNameFromSetter(Method maybeSetter) {
        Matcher matcher = SETTER.matcher(maybeSetter.getName());
        if (!matcher.find()) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but method name is not a JavaBean setter.");
        }

        String raw = matcher.group(1);
        return Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
    }

    public ConfigMetadataNode compile(String name, Type type) {
        return compile(new Descriptor(name, type));
    }

    protected ConfigMetadataNode compile(Descriptor descriptor) {

        Class<?> typeClass = descriptor.getTypeClass();

        if (Collection.class.isAssignableFrom(typeClass)) {
            return compileCollectionMetadata(descriptor);
        } else if (Map.class.isAssignableFrom(typeClass)) {
            return compileMapMetadata(descriptor);
        } else if (typeClass.isAnnotationPresent(BQConfig.class)) {
            return compileObjectMetadata(descriptor);
        } else {
            return compileValueMetadata(descriptor);
        }
    }

    protected ConfigMetadataNode compileObjectMetadata(final Descriptor descriptor) {

        // see if there's already a metadata object for this type... proxy it to avoid compile cycles...
        final ConfigMetadataNode seenNode = seen.get(descriptor.getType());

        // create an empty object ourselves, as we need to cache it before we descend down the stack to prevent
        // endless cycles during compilation... note that we are only caching inside 'compileObjectMetadata'...
        // That's the place to break the cycles..
        final ConfigObjectMetadata baseObject = new ConfigObjectMetadata();
        seen.put(descriptor.getType(), baseObject);
        final ConfigObjectMetadata.Builder builder = ConfigObjectMetadata
                .builder(baseObject)
                .name(descriptor.getName())
                .type(descriptor.getType())
                .abstractType(isAbstract(descriptor.getTypeClass()))
                .typeLabel(extractTypeLabel(descriptor.getTypeClass()));

        if (seenNode == null) {
            // note that root config object known to Bootique doesn't require BQConfig annotation (though it would help in
            // determining description). Objects nested within the root config do. Otherwise they will be treated as
            // "value" properties.
            final BQConfig typeAnnotation = descriptor.getTypeClass().getAnnotation(BQConfig.class);
            if (typeAnnotation != null) {
                builder.description(typeAnnotation.value());
            }

            for (Method m : descriptor.getTypeClass().getMethods()) {
                final BQConfigProperty configProperty = m.getAnnotation(BQConfigProperty.class);
                if (configProperty != null) {
                    final Type propType = propertyTypeFromSetter(m);
                    builder.addProperty(compile(new Descriptor(propertyNameFromSetter(m), configProperty, propType)));
                }
            }

            for (Constructor<?> c : descriptor.getTypeClass().getConstructors()) {
                final BQConfigProperty configProperty = c.getAnnotation(BQConfigProperty.class);
                if (configProperty != null) {
                    final Type propType = propertyTypeFromConstructor(c);
                    builder.addProperty(compile(new Descriptor(descriptor.getTypeClass().getSimpleName().toLowerCase(), configProperty, propType)));
                }
            }
        }

        // compile subconfigs...
        subclassProvider.apply(descriptor.getTypeClass())
                .map(sc -> new Descriptor(null, sc))
                .map(this::compileObjectMetadata)
                .forEach(builder::addSubConfig);

        return builder.build();
    }

    protected String extractTypeLabel(Class<?> type) {
        // TODO: get rid of Jackson annotations dependency .. devise our own that reflect Bootique style of config factory
        // subclassing...

        JsonTypeName typeName = type.getAnnotation(JsonTypeName.class);
        return typeName != null ? typeName.value() : null;
    }

    protected boolean isAbstract(Class<?> type) {
        int modifiers = type.getModifiers();
        return Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers);
    }

    protected ConfigMetadataNode compileValueMetadata(Descriptor descriptor) {
        ValueObjectDescriptor valueObjectDescriptor = descriptorMap.get(descriptor.typeClass);

        return ConfigValueMetadata
                .builder(descriptor.getName())
                .type(descriptor.getType())
                .description(descriptor.getDescription())
                .valueLabel(valueObjectDescriptor != null ? valueObjectDescriptor.getDescription() : null)
                .build();
    }

    protected ConfigMetadataNode compileMapMetadata(Descriptor descriptor) {

        Type type = descriptor.getType();
        Class<?> keysType = Object.class;
        Type valuesType = Object.class;

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] args = parameterizedType.getActualTypeArguments();

            if (args.length == 2) {
                if (args[0] instanceof Class) {
                    keysType = (Class<?>) args[0];
                }

                valuesType = args[1];
            }
        }

        ConfigMetadataNode valueMetadata = compile(new Descriptor(null, valuesType));

        return ConfigMapMetadata
                .builder(descriptor.getName())
                .type(type)
                .description(descriptor.getDescription())
                .keysType(keysType)
                .valuesType(valueMetadata)
                .build();
    }

    protected ConfigMetadataNode compileCollectionMetadata(Descriptor descriptor) {

        Type type = descriptor.getType();
        Type elementType = Object.class;

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] args = parameterizedType.getActualTypeArguments();

            if (args.length == 1) {
                elementType = args[0];
            }
        }

        ConfigMetadataNode elementMetadata = compile(new Descriptor(null, elementType));
        return ConfigListMetadata
                .builder(descriptor.getName())
                .type(type)
                .description(descriptor.getDescription())
                .elementType(elementMetadata)
                .build();
    }

    protected static class Descriptor {

        private String name;
        private String description;
        private Class<?> typeClass;
        private Type type;

        // describe type that is another type's property using property descriptor
        public Descriptor(String name, BQConfigProperty description, Type type) {
            this.type = type;
            this.typeClass = typeClass(type);

            this.name = name;
            if (description != null) {
                this.description = description.value();
            }
        }

        // describe root type that using type descriptor
        public Descriptor(String name, Type type) {
            this.type = type;
            this.typeClass = typeClass(type);
            this.name = name;

            BQConfig config = typeClass.getAnnotation(BQConfig.class);
            if (config != null) {
                this.description = config.value();
            }
        }

        private static Class<?> typeClass(Type type) {

            if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType() instanceof Class) {
                    return (Class<?>) parameterizedType.getRawType();
                }
            }

            return Object.class;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Class<?> getTypeClass() {
            return typeClass;
        }

        public Type getType() {
            return type;
        }
    }
}
