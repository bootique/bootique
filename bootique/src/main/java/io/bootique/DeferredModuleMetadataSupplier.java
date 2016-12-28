package io.bootique;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModuleMetadata;

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

    private static ConfigValueMetadata compile(Type type, String name, BQConfigProperty propertyAnnotation) {

        Class<?> typeClass = Object.class;
        Type[] parameterTypes = null;

        if (type instanceof Class) {
            typeClass = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {

            ParameterizedType parameterizedType = (ParameterizedType) type;

            if (parameterizedType.getRawType() instanceof Class) {
                typeClass = (Class<?>) parameterizedType.getRawType();
                parameterTypes = parameterizedType.getActualTypeArguments();
            }
        }

        if (Collection.class.isAssignableFrom(typeClass)) {
            return toCollectionProperty(name, typeClass, parameterTypes, propertyAnnotation);
        } else if (Map.class.isAssignableFrom(typeClass)) {
            return toMapProperty(name, typeClass, parameterTypes, propertyAnnotation);
        } else if (typeClass.isAnnotationPresent(BQConfig.class)) {
            return toConfig(name, typeClass);
        } else {
            return toConfigProperty(name, typeClass, propertyAnnotation);
        }
    }

    private static ConfigListMetadata toCollectionProperty(
            String name,
            Class<?> collectionType,
            Type[] collectionParameters,
            BQConfigProperty annotation) {

        Class<?> elementType = collectionParameters != null
                && collectionParameters.length == 1
                && collectionParameters[0] instanceof Class
                ? (Class<?>) collectionParameters[0] : Object.class;

        ConfigValueMetadata elementMetadata = compile(elementType, null, null);

        return ConfigListMetadata
                .builder(name)
                .type(collectionType)
                .description(annotation.value())
                .elementType(elementMetadata)
                .build();
    }

    private static ConfigMapMetadata toMapProperty(
            String name,
            Class<?> mapType,
            Type[] mapParameters,
            BQConfigProperty annotation) {

        Class<?> keysType = Object.class;
        Class<?> valuesType = Object.class;

        if (mapParameters != null && mapParameters.length == 2) {

            if (mapParameters[0] instanceof Class) {
                keysType = (Class<?>) mapParameters[0];
            }

            if (mapParameters[1] instanceof Class) {
                valuesType = (Class<?>) mapParameters[1];
            }
        }

        ConfigValueMetadata valueMetadata = compile(valuesType, null, null);

        return ConfigMapMetadata
                .builder(name)
                .type(mapType)
                .description(annotation.value())
                .keysType(keysType)
                .valuesType(valueMetadata)
                .build();
    }

    private static ConfigValueMetadata toConfigProperty(String name, Class<?> type, BQConfigProperty annotation) {
        return ConfigValueMetadata
                .builder(name)
                .type(type)
                .description(annotation != null ? annotation.value() : null)
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
}
