package io.bootique;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.module.ConfigListMetadata;
import io.bootique.module.ConfigObjectMetadata;
import io.bootique.module.ConfigPropertyMetadata;
import io.bootique.module.ModuleMetadata;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

class DeferredModuleMetadataSupplier implements Supplier<Collection<ModuleMetadata>> {

    private static final Pattern SETTER = Pattern.compile("^set([A-Z].*)$");

    private Collection<BQModule> modules;

    static ConfigObjectMetadata toConfig(String name, Class<?> type) {

        ConfigObjectMetadata.Builder builder = ConfigObjectMetadata.builder(name).type(type);

        // note that root config object known to Bootique doesn't require BQConfig annotation (though it would help in
        // determining description). Objects nested within the root config do. Otherwise they will be treated as
        // "simple" properties.
        BQConfig configAnnotation = type.getAnnotation(BQConfig.class);
        if (configAnnotation != null) {
            builder.description(configAnnotation.value());
        }

        for (Method m : type.getMethods()) {
            BQConfigProperty configPropertyAnnotation = m.getAnnotation(BQConfigProperty.class);
            if (configPropertyAnnotation != null) {
                builder.addProperty(compile(propertyTypeFromSetter(m), propertyNameFromSetter(m), configPropertyAnnotation));
            }
        }

        return builder.build();
    }

    private static ConfigPropertyMetadata compile(Type type, String name, BQConfigProperty propertyAnnotation) {

        Class<?> typeClass = Object.class;
        Class<?> parameterType = Object.class;

        if (type instanceof Class) {
            typeClass = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            if (parameterizedType.getRawType() instanceof Class) {
                typeClass = (Class<?>) parameterizedType.getRawType();
            }

            Type[] args = parameterizedType.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class) {
                parameterType = (Class<?>) args[0];
            }
        }

        if (Collection.class.isAssignableFrom(typeClass)) {
            return toCollectionProperty(name, typeClass, parameterType, propertyAnnotation);
        } else if (typeClass.isAnnotationPresent(BQConfig.class)) {
            return toConfig(name, typeClass);
        } else {
            return toConfigProperty(name, typeClass, propertyAnnotation);
        }
    }

    private static ConfigListMetadata toCollectionProperty(
            String name,
            Class<?> collectionType,
            Class<?> elementType,
            BQConfigProperty annotation) {

        ConfigPropertyMetadata elementMetadata = ConfigPropertyMetadata.builder().type(elementType).build();

        return ConfigListMetadata
                .builder(name)
                .type(collectionType)
                .description(annotation.value())
                .elementType(elementMetadata)
                .build();
    }

    private static ConfigPropertyMetadata toConfigProperty(String name, Class<?> type, BQConfigProperty annotation) {
        return ConfigPropertyMetadata
                .builder(name)
                .type(type)
                .description(annotation.value())
                .build();
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

    @Override
    public Collection<ModuleMetadata> get() {

        if (modules == null) {
            throw new IllegalStateException("DeferredModuleMetadataSupplier is not initialized");
        }

        return modules.stream().map(this::toModuleMetadata).collect(toList());
    }

    // this method must be called exactly once before 'get' can be invoked....
    void init(Collection<BQModule> modules) {

        if (this.modules != null) {
            throw new IllegalStateException("DeferredModuleMetadataSupplier is already initialized");
        }

        this.modules = Objects.requireNonNull(modules);
    }

    private ModuleMetadata toModuleMetadata(BQModule module) {
        return ModuleMetadata
                .builder(module.getName())
                .description(module.getDescription())
                .addConfigs(toConfigs(module))
                .build();
    }

    private Collection<ConfigObjectMetadata> toConfigs(BQModule module) {

        Map<String, Class<?>> configTypes = module.getConfigs();
        if (configTypes.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<ConfigObjectMetadata> configs = new ArrayList<>();

        configTypes.forEach((prefix, type) -> {
            configs.add(toConfig(prefix, type));
        });

        return configs;
    }

    private enum PropertyType {

        scalar, object, collection
    }
}
