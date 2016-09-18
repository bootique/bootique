package io.bootique;

import com.google.inject.Module;

/**
 * @since 0.10
 */
public interface BQModuleOverrideBuilder<T> {

	T with(Class<? extends Module> moduleType);

	/**
	 * @since 0.12
	 * @param module
	 *            overrding Module.
	 * @return {@link Bootique} instance we are configuring.
	 */
	T with(Module module);
}
