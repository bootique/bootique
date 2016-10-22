package io.bootique;

import io.bootique.module.ModuleMetadata;

import java.util.Collection;
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

        return modules.stream().map(this::toMetadata).collect(toList());
    }

    // this method must be called exactly once before 'get' can be invoked....
    void init(Collection<BQModule> modules) {

        if (this.modules != null) {
            throw new IllegalStateException("DeferredModuleMetadataSupplier is already initialized");
        }

        this.modules = Objects.requireNonNull(modules);
    }

    private ModuleMetadata toMetadata(BQModule module) {
        // TODO: description, config objects....
        return ModuleMetadata.builder().name(module.getName()).build();
    }
}
