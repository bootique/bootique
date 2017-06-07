package io.bootique;

import com.google.inject.Module;

import java.util.ArrayList;
import java.util.Collection;

class RuntimeModule {

    private BQModule bqModule;
    private RuntimeModule overridesThis;
    private Collection<RuntimeModule> overridden;

    RuntimeModule(BQModule bqModule) {
        this.bqModule = bqModule;
        this.overridden = new ArrayList<>(3);
    }

    @Override
    public boolean equals(Object object) {

        if (object == this) {
            return true;
        }

        if (object instanceof RuntimeModule) {

            RuntimeModule otherRuntimeModule = (RuntimeModule) object;

            // equality by module type...
            return getModule().getClass().equals(otherRuntimeModule.getModule().getClass());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 37 + getModule().getClass().hashCode();
    }

    public Module getModule() {
        return bqModule.getModule();
    }

    public BQModule getBqModule() {
        return bqModule;
    }

    public Collection<RuntimeModule> getOverridden() {
        return overridden;
    }

    void checkCycles() {
        if(overridesThis != null) {
            overridesThis.checkCycles(this);
        }
    }

    private void checkCycles(RuntimeModule root) {
        if (root == this) {
            // TODO: show all modules participating in the detected cycle...
            throw new BootiqueException(1,
                    "Circular override dependency between DI modules. Culprit: " + getModuleName());
        }

        if(overridesThis != null) {
            overridesThis.checkCycles(root);
        }
    }

    Class<? extends Module> getModuleType() {
        return getModule().getClass();
    }

    String getModuleName() {
        return bqModule.getName();
    }

    String getProviderName() {
        return bqModule.getProviderName();
    }

    boolean isNotOverridden() {
        return overridesThis == null;
    }

    void isOverriddenBy(RuntimeModule module) {

        // no more than one override is allowed
       if(this.overridesThis != null) {
           String message = String.format(
                   "Module %s provided by %s is overridden twice by %s and %s",
                   getModuleName(),
                   getProviderName(),
                   this.overridesThis.getModuleName(),
                   module.getModuleName());

           throw new BootiqueException(1, message);
       }

       this.overridesThis = module;
    }

    void overrides(RuntimeModule module) {
        this.overridden.add(module);
    }
}
