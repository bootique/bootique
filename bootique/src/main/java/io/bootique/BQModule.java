package io.bootique;

import com.google.inject.Module;
import io.bootique.names.ClassToName;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Bootique application module. A thin wrapper around Guice DI module that helps Bootique to manage module metadata
 * and override dependencies.
 *
 * @since 0.21
 */
public class BQModule {

    // for now module names are simple class names... maybe change this to use Maven module names?
    protected static ClassToName MODULE_NAME_BUILDER = ClassToName
            .builder()
            .build();

    private Module module;
    private String name;
    private String description;
    private String providerName;
    private Collection<Class<? extends Module>> overrides;
    private Map<String, Class<?>> configs;

    private BQModule() {
    }

    public static Builder builder(Module module) {
        return new Builder(module);
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProviderName() {
        return providerName;
    }

    public Collection<Class<? extends Module>> getOverrides() {
        return overrides;
    }

    public Map<String, Class<?>> getConfigs() {
        return configs;
    }

    public static class Builder {
        private BQModule module;

        private Builder(Module module) {
            this.module = new BQModule();
            this.module.module = Objects.requireNonNull(module);
        }

        public BQModule build() {

            if(module.name == null) {
                module.name = MODULE_NAME_BUILDER.toName(module.module.getClass());
            }

            return module;
        }

        public Builder name(String name) {
            module.name = name;
            return this;
        }

        public Builder description(String descrption) {
            module.description = descrption;
            return this;
        }

        public Builder providerName(String name) {
            module.providerName = name;
            return this;
        }

        public Builder overrides(Collection<Class<? extends Module>> overrides) {
            module.overrides = overrides;
            return this;
        }

        public Builder configs(Map<String, Class<?>> configs) {
            module.configs = configs;
            return this;
        }
    }
}
