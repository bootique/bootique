package io.bootique;

import com.google.inject.Module;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class RuntimeModule {

    private BQModule module;
    private Collection<RuntimeModule> overriddenBy;

    RuntimeModule(BQModule module) {
        this.module = module;
        this.overriddenBy = new LinkedHashSet<>();
    }

    @Override
    public boolean equals(Object object) {

        if (object == this) {
            return true;
        }

        if (object instanceof RuntimeModule) {

            RuntimeModule otherRuntimeModule = (RuntimeModule) object;

            // equality by module type...
            return module.getModule().getClass().equals(otherRuntimeModule.module.getModule().getClass());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 37 + module.getClass().hashCode();
    }

    public Module getModule() {
        return module.getModule();
    }

    Collection<RuntimeModule> getModuleOverrides(Map<Class<? extends Module>, RuntimeModule> modules) {
        return module.getOverrides().stream().map(t -> modules.get(t)).filter(n -> n != null).collect(toList());
    }

    void checkCycles() {
        overriddenBy.forEach(n -> n.checkCycles(this));
    }

    void checkCycles(RuntimeModule root) {
        if (root == this) {
            throw new RuntimeException("Circular override dependency: " + getModuleName());
        }

        overriddenBy.forEach(n -> n.checkCycles(root));
    }

    Class<? extends Module> getModuleType() {
        return module.getModule().getClass();
    }

    String getModuleName() {
        return module.getName();
    }

    String getProviderName() {
        return module.getProviderName();
    }

    Collection<RuntimeModule> getOverriddenBy() {
        return overriddenBy;
    }

    void addOverriddenBy(RuntimeModule module) {
        overriddenBy.add(module);
    }
}
