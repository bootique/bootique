package io.bootique;

import java.util.Objects;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * A Bootique-specific Guice Module that sets up a one or another application
 * subsystem. It is intended as a superclass for Modules that are centered
 * around a single YAML configuration, so it is used in Bootique integration
 * modules, etc.
 * 
 * @since 0.9
 */
public abstract class ConfigModule implements Module {

	protected String configPrefix;

	public ConfigModule() {
		init(defaultConfigPrefix());
	}

	public ConfigModule(String configPrefix) {
		init(configPrefix);
	}

	private void init(String configPrefix) {
		Objects.requireNonNull(configPrefix);
		this.configPrefix = configPrefix;
	}

	/**
	 * Does nothing and is intended for optional overriding.
	 */
	@Override
	public void configure(Binder binder) {
		// do nothing
	}

	protected String defaultConfigPrefix() {
		String name = getClass().getSimpleName().toLowerCase();
		final String stripSuffix = "module";
		return (name.endsWith(stripSuffix)) ? name.substring(0, name.length() - stripSuffix.length()) : name;
	}

}
