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
import io.bootique.log.BootLogger;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A thread-safe caching compiler of configuration metadata for objects annotated with {@link BQConfig} and
 * {@link BQConfigProperty}.
 */
public class ConfigMetadataCompiler {

    private static final Pattern SETTER = Pattern.compile("^set([A-Z].*)$");

    private final BootLogger logger;
    private final Function<Class<?>, Stream<Class<?>>> subclassProvider;
    private final Map<Type, ConfigObjectMetadata> seen;
    private final Map<Class<?>, ValueObjectDescriptor> descriptorMap;

    public ConfigMetadataCompiler(
            BootLogger logger,
            Function<Class<?>, Stream<Class<?>>> subclassProvider,
            Map<Class<?>, ValueObjectDescriptor> descriptorMap) {

        this.logger = logger;
        this.subclassProvider = subclassProvider;
        this.descriptorMap = descriptorMap;
        this.seen = new ConcurrentHashMap<>();
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

    private static Type propertyTypeFromDelegatedConstructor(Constructor maybeConstructor) {
        Type[] paramTypes = maybeConstructor.getGenericParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalStateException("Constructor '" + maybeConstructor
                    + "' is annotated with @BQConfig, but does not match the expected delegated constructor signature."
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
        return compile(Descriptor.forRootConfig(name, type));
    }

    protected ConfigMetadataNode compile(Descriptor descriptor) {

        Class<?> typeClass = descriptor.getTypeClass();

        if (Collection.class.isAssignableFrom(typeClass)) {
            return compileCollectionMetadata(descriptor);
        } else if (Map.class.isAssignableFrom(typeClass)) {
            return compileMapMetadata(descriptor);
        } else if (typeClass.isAnnotationPresent(BQConfig.class)) {
            ConfigMetadataNode delegated = compileDelegatedConstructorMetadata(descriptor);
            return delegated != null ? delegated : compileObjectMetadata(descriptor);
        } else {
            ConfigMetadataNode delegated = compileDelegatedConstructorMetadata(descriptor);
            return delegated != null ? delegated : compileValueMetadata(descriptor);
        }
    }

    protected ConfigMetadataNode compileObjectMetadata(Descriptor descriptor) {

        // to avoid compile cycles, track seen types and prevent any further descent into child properties for them.
        // Still use the name and description from the given Descriptor.
        ConfigObjectMetadata seenNode = seen.get(descriptor.getType());
        if (seenNode != null) {
            return ConfigObjectMetadata
                    .builder(new ConfigObjectMetadata())
                    .from(seenNode)
                    .name(descriptor.getName())
                    .description(descriptor.getDescription())
                    .build();
        }

        // Cache the metadata object before we descend the stack to prevent endless cycles during compilation.
        // We are only caching inside 'compileObjectMetadata'. This is a designated place to break cycles.

        ConfigObjectMetadata baseObject = new ConfigObjectMetadata();
        seen.put(descriptor.getType(), baseObject);
        ConfigObjectMetadata.Builder builder = ConfigObjectMetadata
                .builder(baseObject)
                .name(descriptor.getName())
                .type(descriptor.getType())
                .abstractType(isAbstract(descriptor.getTypeClass()))
                .typeLabel(extractTypeLabel(descriptor.getTypeClass()));


        // The root config object known to Bootique doesn't require BQConfig annotation (though it would help in
        // determining description). Objects nested within the root config do. Otherwise, they will be treated as
        // "value" properties.
        BQConfig typeAnnotation = descriptor.getTypeClass().getAnnotation(BQConfig.class);
        if (typeAnnotation != null) {
            builder.description(typeAnnotation.value());
        }

        for (Method m : descriptor.getTypeClass().getMethods()) {
            BQConfigProperty configProperty = m.getAnnotation(BQConfigProperty.class);
            if (configProperty != null) {
                Type propType = propertyTypeFromSetter(m);
                builder.addProperty(compile(Descriptor.forNestedConfig(propertyNameFromSetter(m), configProperty, propType)));
            }
        }

        // TODO: what if multiple annotated constructors are encountered? Currently, adding them all together
        for (Constructor<?> c : descriptor.getTypeClass().getConstructors()) {

            Parameter[] params = c.getParameters();
            int len = params.length;

            Descriptor[] descriptors = null;
            Type[] types = null;
            int i = 0;
            for (; i < len; i++) {

                BQConfigProperty configProperty = params[i].getAnnotation(BQConfigProperty.class);
                if (configProperty == null) {
                    break;
                }

                if ("".equals(configProperty.property())) {
                    logger.stderr("Ignoring @BQConfigProperty on constructor parameter of " + descriptor.getTypeClass() + ", as it has no 'property' set");
                    break;
                }

                if (i == 0) {
                    descriptors = new Descriptor[len];
                    types = c.getGenericParameterTypes();
                }

                descriptors[i] = Descriptor.forConstructorParamConfig(configProperty, types[i]);
            }

            // only load constructor properties if all Constructor parameters are annotated
            if (descriptors != null && i == len) {
                for (Descriptor d : descriptors) {
                    builder.addProperty(compile(d));
                }
            }
        }

        // compile subconfigs...
        subclassProvider.apply(descriptor.getTypeClass())
                .map(sc -> Descriptor.forRootConfig(null, sc))
                .map(this::compileObjectMetadata)
                .forEach(builder::addSubConfig);

        return builder.build();
    }

    protected ConfigMetadataNode compileDelegatedConstructorMetadata(Descriptor descriptor) {
        // check for "delegated" constructors before building anything new
        for (Constructor<?> c : descriptor.getTypeClass().getConstructors()) {
            BQConfig configProperty = c.getAnnotation(BQConfig.class);
            if (configProperty != null) {
                Type propType = propertyTypeFromDelegatedConstructor(c);

                return compile(Descriptor.forDelegatedConfig(descriptor, configProperty, propType));
            }
        }

        // TODO: ^^ report on other matching delegated Constructors?
        return null;
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

        ConfigMetadataNode valueMetadata = compile(Descriptor.forRootConfig(null, valuesType));

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

        ConfigMetadataNode elementMetadata = compile(Descriptor.forRootConfig(null, elementType));
        return ConfigListMetadata
                .builder(descriptor.getName())
                .type(type)
                .description(descriptor.getDescription())
                .elementType(elementMetadata)
                .build();
    }

    protected static class Descriptor {

        private final String name;
        private final String description;
        private final Class<?> typeClass;
        private final Type type;

        public static Descriptor forRootConfig(String name, Type type) {
            Class<?> typeClass = typeClass(type);
            BQConfig config = typeClass.getAnnotation(BQConfig.class);

            return new Descriptor(
                    name,
                    config != null ? config.value() : null,
                    typeClass,
                    type
            );
        }

        public static Descriptor forNestedConfig(String name, BQConfigProperty description, Type type) {
            return new Descriptor(
                    name, // TODO: name from description.property()
                    description != null ? description.value() : null,
                    typeClass(type),
                    type
            );
        }

        public static Descriptor forDelegatedConfig(Descriptor parent, BQConfig description, Type type) {
            Class<?> typeClass = typeClass(type);

            return new Descriptor(
                    parent.name,
                    parent.description != null && !parent.description.isEmpty() ? parent.description : description != null ? description.value() : null,
                    typeClass,
                    type
            );
        }

        public static Descriptor forConstructorParamConfig(BQConfigProperty description, Type type) {

            Objects.requireNonNull(description);

            return new Descriptor(
                    description.property(),
                    description.value(),
                    typeClass(type),
                    type
            );
        }

        protected Descriptor(String name, String description, Class<?> typeClass, Type type) {
            this.name = name;
            this.description = description;
            this.typeClass = typeClass;
            this.type = type;
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
