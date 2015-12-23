package com.nhl.bootique;

import com.google.inject.Module;

/**
 * @since 0.10
 */
public interface BQModuleOverrideBuilder {

	Bootique with(Class<? extends Module> moduleType);
}
