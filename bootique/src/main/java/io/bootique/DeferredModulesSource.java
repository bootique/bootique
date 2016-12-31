package io.bootique;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

class DeferredModulesSource implements Supplier<Collection<BQModule>> {

    private Collection<BQModule> modules;

    @Override
    public Collection<BQModule> get() {

        if (modules == null) {
            throw new IllegalStateException("DeferredModuleMetadataSupplier is not initialized");
        }

        return modules;
    }

    // this method must be called exactly once before 'get' can be invoked....
    void init(Collection<BQModule> modules) {

        if (this.modules != null) {
            throw new IllegalStateException("DeferredModuleMetadataSupplier is already initialized");
        }

        this.modules = Objects.requireNonNull(modules);
    }
}
