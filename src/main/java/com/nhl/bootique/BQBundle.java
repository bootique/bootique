package com.nhl.bootique;

import com.google.inject.Module;

/**
 * Bundle is a facade to a code modules, existing for the purpose of integrating
 * such module in Bootique. Integration happens via Guice Modules produced by
 * the bundle.
 * 
 * @since 0.8
 */
@FunctionalInterface
public interface BQBundle {

	Module module(String configPrefix);

	default Module module() {
		return module(configPrefix());
	}

	default String configPrefix() {
		String name = getClass().getSimpleName().toLowerCase();
		final String stripSuffix = "bundle";
		return (name.endsWith(stripSuffix)) ? name.substring(0, name.length() - stripSuffix.length()) : name;
	}
}
