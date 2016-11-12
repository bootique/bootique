package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.names.ClassToName;

import java.util.Objects;

/**
 * A Bootique-specific Guice Module that sets up a one or another application
 * subsystem. It is intended as a superclass for Modules that are centered
 * around a single YAML configuration, so it is used in Bootique integration
 * modules, etc.
 * 
 * @since 0.9
 */
public abstract class ConfigModule implements Module {

	protected static ClassToName CONFIG_PREFIX_BUILDER = ClassToName
			.builder()
			.convertToLowerCase()
			.stripSuffix("Module")
			.build();

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
		return CONFIG_PREFIX_BUILDER.toName(getClass());
	}

}
