package io.bootique;

import io.bootique.module.ConfigMetadata;
import io.bootique.module.ModuleMetadata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

class DeferredModuleMetadataSupplier implements Supplier<Collection<ModuleMetadata>> {

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
        // TODO: description?
        return ModuleMetadata
                .builder()
                .name(module.getName())
                .addConfigs(toConfigs(module))
                .build();
    }

    private Map<String, ConfigMetadata> toConfigs(BQModule module) {

        Map<String, Class<?>> configTypes = module.getConfigs();
        if (configTypes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ConfigMetadata> configs = new HashMap<>();

        configTypes.forEach((prefix, type) -> {

            // TODO: compile annotations of config objects, discover properties
            ConfigMetadata config = ConfigMetadata
                    .builder()
                    .name(type.getSimpleName()).build();
            configs.put(prefix, config);
        });

        return configs;
    }
}
