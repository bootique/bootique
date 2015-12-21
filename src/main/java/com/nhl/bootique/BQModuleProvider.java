package com.nhl.bootique;

import java.util.Optional;
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
	 * Returns an Optional with the type of the module overridden by this
	 * Module.
	 * 
	 * @since 0.10
	 * @return an Optional that may contain a type of the Module replaced by
	 *         this Module.
	 */
	default Optional<Class<? extends Module>> replaces() {
		return Optional.empty();
	}
}
