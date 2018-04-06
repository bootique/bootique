package io.bootique.unit;

import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.BQModuleId;
import io.bootique.BQModuleOverrideBuilder;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.log.BootLogger;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BQInternalTestFactory extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BQInternalTestFactory.class);

    protected Collection<BQRuntime> runtimes;

    @Override
    protected void after() {

        LOGGER.info("Stopping runtime...");

        Collection<BQRuntime> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.forEach(runtime -> {
                try {
                    runtime.shutdown();
                } catch (Exception e) {
                    // ignore...
                }
            });
        }
    }

    @Override
    protected void before() {
        this.runtimes = new ArrayList<>();
    }

    public Builder app(String... args) {
        return new Builder(runtimes, args);
    }

    public static class Builder<T extends Builder<T>> {

        private Collection<BQRuntime> runtimes;
        private Bootique bootique;
        private Map<String, String> properties;
        private Map<String, String> variables;
        private Map<String, String> declaredVars;

        protected Builder(Collection<BQRuntime> runtimes, String[] args) {
            this.runtimes = runtimes;
            this.properties = new HashMap<>();
            this.variables = new HashMap<>();
            this.declaredVars = new HashMap<>();
            this.bootique = Bootique.app(args).module(createPropertiesProvider()).module(createVariablesProvider());
        }

        protected BQModuleProvider createPropertiesProvider() {
            return new BQModuleProvider() {
                private final Module module = binder -> BQCoreModule.extend(binder).setProperties(properties);

                @Override
                public Module module() {
                    return module;
                }

                @Override
                public BQModule.Builder moduleBuilder() {
                    return BQModuleProvider.super
                            .moduleBuilder()
                            .name("BQInternalTestFactory:Builder:properties");
                }

                @Override
                public String name() {
                    return "BQInternalTestFactory:Builder:properties:provider";
                }

                @Override
                public BQModuleId id() {
                    return BQModuleId.of(module);
                }
            };
        }

        protected BQModuleProvider createVariablesProvider() {
            return new BQModuleProvider() {
                private final Module module = binder -> BQCoreModule.extend(binder).setVars(variables).declareVars(declaredVars);

                @Override
                public Module module() {
                    return module;
                }

                @Override
                public BQModule.Builder moduleBuilder() {
                    return BQModuleProvider.super
                            .moduleBuilder()
                            .name("BQInternalTestFactory:Builder:variables");
                }

                @Override
                public String name() {
                    return "BQInternalTestFactory:Builder:variables:provider";
                }

                @Override
                public BQModuleId id() {
                    return BQModuleId.of(module);
                }
            };
        }

        public T property(String key, String value) {
            properties.put(key, value);
            return (T) this;
        }

        public T var(String key, String value) {
            variables.put(key, value);
            return (T) this;
        }

        public T declareVar(String path, String var) {
            declaredVars.put(path, var);
            return (T) this;
        }

        public T args(String... args) {
            bootique.args(args);
            return (T) this;
        }

        public T args(Collection<String> args) {
            bootique.args(args);
            return (T) this;
        }

        public T autoLoadModules() {
            bootique.autoLoadModules();
            return (T) this;
        }

        public T module(Class<? extends Module> moduleType) {
            bootique.module(moduleType);
            return (T) this;
        }

        public T modules(Class<? extends Module>... moduleTypes) {
            bootique.modules(moduleTypes);
            return (T) this;
        }

        public T module(Module m) {
            bootique.module(m);
            return (T) this;
        }

        public T modules(Module... modules) {
            bootique.modules(modules);
            return (T) this;
        }

        public T module(BQModuleProvider moduleProvider) {
            bootique.module(moduleProvider);
            return (T) this;
        }

        public T bootLogger(BootLogger bootLogger) {
            bootique.bootLogger(bootLogger);
            return (T) this;
        }

        public BQModuleOverrideBuilder<T> override(Class<? extends Module>... overriddenTypes) {

            BQModuleOverrideBuilder<Bootique> subBuilder = bootique.override(overriddenTypes);
            return new BQModuleOverrideBuilder<T>() {

                @Override
                public T with(Class<? extends Module> moduleType) {
                    subBuilder.with(moduleType);
                    return (T) Builder.this;
                }

                @Override
                public T with(Module module) {
                    subBuilder.with(module);
                    return (T) Builder.this;
                }
            };
        }

        public BQRuntime createRuntime() {
            BQRuntime runtime = bootique.createRuntime();
            runtimes.add(runtime);
            return runtime;
        }
    }
}
