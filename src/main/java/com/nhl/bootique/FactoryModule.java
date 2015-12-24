package com.nhl.bootique;

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.nhl.bootique.factory.FactoryConfigurationService;

/**
 * A Bootique-specific Guice Module that sets up a one or another application
 * subsystem. It is intended as a superclass for Modules that are centered
 * around a single YAML-configurable factory, so it is used in Bootique
 * integration modules, etc.
 * 
 * @since 0.8
 * @deprecated since 0.9 inherit from {@link ConfigModule} or {@link Module}.
 */
@Deprecated
public abstract class FactoryModule<C> implements Module {

	protected Class<C> factoryType;
	protected String configPrefix;

	public FactoryModule(Class<C> factoryType) {
		init(factoryType, defaultConfigPrefix());
	}

	public FactoryModule(Class<C> factoryType, String configPrefix) {
		init(factoryType, configPrefix);
	}

	private void init(Class<C> factoryType, String configPrefix) {
		Preconditions.checkNotNull(factoryType);
		this.factoryType = factoryType;
		this.configPrefix = configPrefix;
	}

	/**
	 * Does nothing and is inteneded for overriding.
	 */
	@Override
	public void configure(Binder binder) {
		// do nothing
	}

	protected C createFactory(FactoryConfigurationService configurationService) {
		Objects.requireNonNull(configPrefix);
		return configurationService.factory(factoryType, configPrefix);
	}

	protected String defaultConfigPrefix() {
		String name = getClass().getSimpleName().toLowerCase();
		final String stripSuffix = "module";
		return (name.endsWith(stripSuffix)) ? name.substring(0, name.length() - stripSuffix.length()) : name;
	}

}
