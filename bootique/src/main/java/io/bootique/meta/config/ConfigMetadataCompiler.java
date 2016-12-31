package io.bootique.meta.config;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A thread-safe caching compiler of configuration metadata for objects annotated with {@link BQConfig} and
 * {@link BQConfigProperty}.
 *
 * @since 0.21
 */
public class ConfigMetadataCompiler {

    private static final Pattern SETTER = Pattern.compile("^set([A-Z].*)$");

    private Map<Type, ConfigMetadataNode> seen;

    public ConfigMetadataCompiler() {
        this.seen = new ConcurrentHashMap<>();
    }

    private static Type propertyTypeFromSetter(Method maybeSetter) {

        if (!Void.TYPE.equals(maybeSetter.getReturnType())) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but does not match expected setter signature."
                    + " It must be void.");
        }

        Type[] paramTypes = maybeSetter.getGenericParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but does not match expected setter signature."
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

    public ConfigMetadataNode compile(String name, Class<?> type) {
        return compile(new Descriptor(name, type));
    }

    protected ConfigMetadataNode compile(Descriptor descriptor) {

        // see if there's already a metadata object for this type... proxy it to avoid compile cycles...
        ConfigMetadataNode seenNode = seen.get(descriptor.getType());
        if (seenNode != null) {
            return new ConfigMetadataNodeProxy(descriptor.getName(), descriptor.getDescription(), seenNode);
        }

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

    protected <T extends ConfigMetadataNode> T createAndCache(Type type, Function<Type, T> factory) {
        // TODO: check for existing objects and throw?
        return (T) seen.computeIfAbsent(type, factory);
    }

    protected ConfigMetadataNode compileObjectMetadata(Descriptor descriptor) {

        // create an empty object ourselves, as we need to cache it before we descend down the stack to prevent
        // endless cycles during compilation...

        // note that we are only caching ConfigObjectMetadata... That's the place where cycles can occur.
        ConfigObjectMetadata baseObject = createAndCache(descriptor.getType(), t -> new ConfigObjectMetadata());
        ConfigObjectMetadata.Builder builder = ConfigObjectMetadata
                .builder(baseObject)
                .name(descriptor.getName())
                .type(descriptor.getType());

        // note that root config object known to Bootique doesn't require BQConfig annotation (though it would help in
        // determining description). Objects nested within the root config do. Otherwise they will be treated as
        // "value" properties.
        BQConfig typeAnnotation = descriptor.getTypeClass().getAnnotation(BQConfig.class);
        if (typeAnnotation != null) {
            builder.description(typeAnnotation.value());
        }

        for (Method m : descriptor.getTypeClass().getMethods()) {
            BQConfigProperty configProperty = m.getAnnotation(BQConfigProperty.class);
            if (configProperty != null) {
                Type propType = propertyTypeFromSetter(m);
                builder.addProperty(compile(new Descriptor(propertyNameFromSetter(m), configProperty, propType)));
            }
        }

        return builder.build();
    }

    protected ConfigMetadataNode compileValueMetadata(Descriptor descriptor) {
        return ConfigValueMetadata
                .builder(descriptor.getName())
                .type(descriptor.getType())
                .description(descriptor.getDescription())
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
