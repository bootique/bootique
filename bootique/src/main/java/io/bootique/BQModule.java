package io.bootique;

import com.google.inject.Module;

import java.util.Collection;
import java.util.Map;

/**
 * Bootique application module. A thin wrapper around Guice DI module that helps Bootique to manage module metadata
 * and override dependencies.
 *
 * @since 0.21
 */
public class BQModule {

    private Module module;
    private String name;
    private String providerName;
    private Collection<Class<? extends Module>> overrides;
    private Map<String, Class<?>> configs;

    private BQModule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
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

        private Builder() {
            this.module = new BQModule();
        }

        public BQModule build() {
            return module;
        }

        public Builder name(String name) {
            module.name = name;
            return this;
        }

        public Builder providerName(String name) {
            module.providerName = name;
            return this;
        }

        public Builder module(Module module) {
            this.module.module = module;
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
