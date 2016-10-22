package io.bootique;

import com.google.inject.Module;

import java.util.Collection;
import java.util.Collections;

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
     * Returns a new instance of {@link BQModule} specific to this provider.
     *
     * @return a new instance of {@link BQModule} specific to this provider.
     * @since 0.21
     */
    default BQModule bootiqueModule() {
        Module module = module();
        return BQModule
                .builder()
                .name(moduleName(module.getClass()))
                .module(module)
                .overrides(overrides())
                .providerName(name())
                .build();
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
     * @return a human readable name of the provider, by default calculated from
     * the class name.
     * @since 0.12
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * @param moduleType Java class of the module.
     * @return human-readable name for a given module type.
     * @since 0.21
     */
    default String moduleName(Class<?> moduleType) {
        return moduleType.getSimpleName();
    }
}
