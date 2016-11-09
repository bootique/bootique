package io.bootique;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.module.ConfigMetadata;
import io.bootique.module.ConfigPropertyMetadata;
import io.bootique.module.ModuleMetadata;

import java.lang.reflect.Method;
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
                .builder()
                .name(module.getName())
                .description(module.getDescription())
                .addConfigs(toConfigs(module))
                .build();
    }

    private Collection<ConfigMetadata> toConfigs(BQModule module) {

        Map<String, Class<?>> configTypes = module.getConfigs();
        if (configTypes.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<ConfigMetadata> configs = new ArrayList<>();

        configTypes.forEach((prefix, type) -> {
            configs.add(toConfig(prefix, type));
        });

        return configs;
    }

    private ConfigMetadata toConfig(String name, Class<?> type) {

        ConfigMetadata.Builder builder = ConfigMetadata.builder()
                .name(name)
                .type(type);

        // note that root config object known to Bootique doesn't require BQConfig annotation (though it would help in
        // determining description). Objects nested within the root config do. Otherwise they will be treated as
        // "simple" properties.
        BQConfig configAnnotation = type.getAnnotation(BQConfig.class);
        if (configAnnotation != null) {
            builder.description(configAnnotation.value());
        }

        for (Method m : type.getMethods()) {
            BQConfigProperty configProperty = m.getAnnotation(BQConfigProperty.class);
            if (configProperty != null) {

                Class<?> propertyType = propertyTypeFromSetter(m);
                String propertyName = propertyNameFromSetter(m);

                // now check if the object is itself a nested config...
                ConfigPropertyMetadata propertyMetadata = propertyType.isAnnotationPresent(BQConfig.class)
                        ? toConfig(propertyName, propertyType)
                        : toConfigProperty(propertyName, propertyType, configProperty);

                builder.addProperty(propertyMetadata);
            }
        }

        return builder.build();
    }

    private ConfigPropertyMetadata toConfigProperty(String name, Class<?> type, BQConfigProperty annotation) {
        return ConfigPropertyMetadata
                .builder()
                .name(name)
                .type(type)
                .description(annotation.value())
                .build();
    }

    private Class<?> propertyTypeFromSetter(Method maybeSetter) {

        if (!Void.TYPE.equals(maybeSetter.getReturnType())) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but does not match expected setter signature."
                    + " It must be void.");
        }

        Class<?>[] paramTypes = maybeSetter.getParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but does not match expected setter signature."
                    + " It must take exactly one parameter");
        }

        return paramTypes[0];
    }

    private String propertyNameFromSetter(Method maybeSetter) {
        Matcher matcher = SETTER.matcher(maybeSetter.getName());
        if (!matcher.find()) {
            throw new IllegalStateException("Method '" + maybeSetter.getDeclaringClass().getName() + "."
                    + maybeSetter.getName() +
                    "' is annotated with @BQConfigProperty, but method name is not a JavaBean setter.");
        }

        String raw = matcher.group(1);
        return Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
    }
}
