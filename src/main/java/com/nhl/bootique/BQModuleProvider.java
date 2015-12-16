package com.nhl.bootique;

import java.util.ServiceLoader;

import com.google.inject.Module;

/**
 * An interface that allows Bootique extensions to be loaded using Java
 * {@link ServiceLoader} mechanism.
 * 
 * @see Bootique#autoLoadExtensions()
 * 
 * @since 0.8
 */
public interface BQModuleProvider {

	/**
	 * Returns a Guice module that is used to configure this provider's backend.
	 */
	Module module();
}
