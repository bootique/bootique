package com.nhl.bootique;

import java.util.Collection;
import java.util.Collections;
import java.util.ServiceLoader;

import com.google.inject.Module;

/**
 * An interface that allows Bootique extensions to be loaded using Java
 * {@link ServiceLoader} mechanism.
 * 
 * @see Bootique#autoLoadModules()
 * 
 * @since 0.8
 */
public interface BQModuleProvider {

	/**
	 * Returns a Guice module that is used to configure this provider's backend.
	 */
	Module module();

	/**
	 * Returns a potentially empty Collection with the types of the module
	 * overridden by this Module.
	 * 
	 * @since 0.10
	 * @return a potentially empty collection of modules overridden by the
	 *         Module created by this provider.
	 */
	default Collection<Class<? extends Module>> overrides() {
		return Collections.emptyList();
	}
}
