package io.bootique;

import com.google.inject.Module;

class RuntimeModule {

    private BQModule bqModule;
    private RuntimeModule overriddenBy;
    private boolean overridesOthers;

    RuntimeModule(BQModule bqModule) {
        this.bqModule = bqModule;
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

    public RuntimeModule getOverriddenBy() {
        return overriddenBy;
    }

    void checkCycles() {
        if(overriddenBy != null) {
            overriddenBy.checkCycles(this);
        }
    }

    private void checkCycles(RuntimeModule root) {
        if (root == this) {
            // TODO: show all modules participating in the detected cycle...
            throw new BootiqueException(1,
                    "Circular override dependency between DI modules. Culprit: " + getModuleName());
        }

        if(overriddenBy != null) {
            overriddenBy.checkCycles(root);
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

    boolean doesNotOverrideOthers() {
        return !overridesOthers;
    }

    public void setOverridesOthers(boolean overridesOthers) {
        this.overridesOthers = overridesOthers;
    }

    void setOverriddenBy(RuntimeModule module) {

        // no more than one override is allowed
       if(this.overriddenBy != null) {
           String message = String.format(
                   "Module %s provided by %s is overridden twice by %s and %s",
                   getModuleName(),
                   getProviderName(),
                   this.overriddenBy.getModuleName(),
                   module.getModuleName());

           throw new BootiqueException(1, message);
       }

       this.overriddenBy = module;
    }
}
