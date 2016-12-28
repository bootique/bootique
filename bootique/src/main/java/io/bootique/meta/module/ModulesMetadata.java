package io.bootique.meta.module;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Metadata object representing
 *
 * @since 0.21
 */
public class ModulesMetadata {

    private Collection<ModuleMetadata> modules;

    private ModulesMetadata() {
        this.modules = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ModuleMetadata module, ModuleMetadata... moreModules) {
        Builder b = builder().addModule(module);

        if (moreModules != null) {
            b.addModules(asList(moreModules));
        }

        return b;
    }

    /**
     * Returns a collection of metadata objects, one for each DI module.
     *
     * @return a collection of application DI modules.
     */
    public Collection<ModuleMetadata> getModules() {
        return modules;
    }

    public static class Builder {

        private ModulesMetadata modules;

        private Builder() {
            this.modules = new ModulesMetadata();
        }

        public ModulesMetadata build() {
            return modules;
        }

        public Builder addModule(ModuleMetadata moduleMetadata) {
            modules.modules.add(moduleMetadata);
            return this;
        }

        public Builder addModules(Collection<ModuleMetadata> moduleMetadata) {
            modules.modules.addAll(moduleMetadata);
            return this;
        }
    }
}
