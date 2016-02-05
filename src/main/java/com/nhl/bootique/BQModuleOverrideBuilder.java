package com.nhl.bootique;

import com.google.inject.Module;

/**
 * @since 0.10
 */
public interface BQModuleOverrideBuilder {

	Bootique with(Class<? extends Module> moduleType);

	/**
	 * @since 0.12
	 * @param module
	 *            overrding Module.
	 * @return {@link Bootique} instance we are configuring.
	 */
	Bootique with(Module module);
}
