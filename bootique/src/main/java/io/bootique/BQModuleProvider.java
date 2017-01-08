package io.bootique;

import com.google.inject.Module;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A provider of a DI module of a given kind. Central to Bootique module auto-loading and metadata discovery.
 * Bootique modules normally supply "META-INF/services/io.bootique.BQModuleProvider" file packaged in the .jar containing
 * the name of the provider.
 *
 * @see Bootique#autoLoadModules()
 * @since 0.8
 */
public interface BQModuleProvider {

    /**
     * Returns a Guice module specific to this provider.
     *
     * @return an instance of a Guice Module specific to this provider.
     */
    Module module();

    /**
     * Returns a new instance of {@link io.bootique.BQModule.Builder} initialized with module for this provider.
     * Subclasses can invoke extra builder methods to provide metadata, etc.
     *
     * @return a new instance of {@link BQModule} specific to this provider.
     * @since 0.21
     */
    default BQModule.Builder moduleBuilder() {
        return BQModule
                .builder(module())
                .overrides(overrides())
                .providerName(name())
                .configs(configs());
    }

    /**
     * A potentially empty map of configuration types supported by this module, keyed by default configuration
     * prefix.
     *
     * @return a potentially empty map of configuration types supported by this module, keyed by default configuration
     * prefix.
     * @since 0.21
     */
    default Map<String, Class<?>> configs() {
        return Collections.emptyMap();
    }

    /**
     * Returns a potentially empty Collection with the types of the module
     * overridden by this Module.
     *
     * @return a potentially empty collection of modules overridden by the
     * Module created by this provider.
     * @since 0.10
     */
    default Collection<Class<? extends Module>> overrides() {
        return Collections.emptyList();
    }

    /**
     * Returns a human readable name of the provider.
     *
     * @return a human readable name of the provider. Equals to the "simple" class name by default.
     * @since 0.12
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
