package io.bootique;

import io.bootique.config.jackson.JsonConfigurationLoader;
import io.bootique.di.Binder;

import java.util.Map;

public class BQCoreModuleUtility extends BQCoreModuleExtender{
    protected BQCoreModuleUtility(Binder binder) {
        super(binder);
    }

    /**
     * Adds a callback that is invoked once BQRuntime is created, but before any commands are run. Used primarily by
     * the test tools and such to manage lifecycles external to Bootique. Most regular apps do not require this callback.
     *
     * @since 3.0.M1
     */
    public BQCoreModuleUtility addRuntimeListener(Class<? extends BQRuntimeListener> listenerTypes) {
        contributeRuntimeListeners().add(listenerTypes).inSingletonScope();
        return this;
    }

    /**
     * Adds an internal strategy for loading configurations. Note that Bootique already comes preconfigured with a
     * comprehensive set of strategies to load configuration from config files and URLs, so you'd rarely need to
     * specify your own strategy.
     *
     * @since 2.0.B1
     */
    public BQCoreModuleUtility addConfigLoader(JsonConfigurationLoader loader) {
        contributeConfigurationLoaders().addInstance(loader);
        return this;
    }
    public BQCoreModuleUtility declareVars(Map<String, String> varsByConfigPaths) {
        varsByConfigPaths.forEach(this::declareVar);
        return this;
    }
}
