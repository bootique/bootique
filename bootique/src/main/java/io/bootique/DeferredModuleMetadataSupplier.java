package io.bootique;

import io.bootique.meta.config.ConfigMetadataCompiler;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.module.ModuleMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

class DeferredModuleMetadataSupplier implements Supplier<Collection<ModuleMetadata>> {

    private Collection<BQModule> modules;
    private ConfigMetadataCompiler configCompiler;

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
        this.configCompiler = new ConfigMetadataCompiler();
    }

    private ModuleMetadata toModuleMetadata(BQModule module) {
        return ModuleMetadata
                .builder(module.getName())
                .description(module.getDescription())
                .addConfigs(toConfigs(module))
                .build();
    }

    private Collection<ConfigMetadataNode> toConfigs(BQModule module) {

        Map<String, Class<?>> configTypes = module.getConfigs();
        if (configTypes.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<ConfigMetadataNode> configs = new ArrayList<>();

        configTypes.forEach((prefix, type) -> {
            configs.add(configCompiler.compile(prefix, type));
        });

        return configs;
    }
}
