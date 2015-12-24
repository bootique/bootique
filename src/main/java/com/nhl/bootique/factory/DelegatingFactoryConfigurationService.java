package com.nhl.bootique.factory;

import java.lang.reflect.Type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.type.TypeRef;

/**
 * @since 0.10
 * @deprecated since 0.10 as {@link FactoryConfigurationService} is deprecated,
 *             this implementation simply delegates to
 *             {@link ConfigurationFactory}.
 */
@Deprecated
public class DelegatingFactoryConfigurationService implements FactoryConfigurationService {

	private ConfigurationFactory configFactory;

	@Inject
	public DelegatingFactoryConfigurationService(ConfigurationFactory configFactory) {
		this.configFactory = configFactory;
	}

	@Override
	public <T> T factory(Class<T> type, String prefix) {
		return configFactory.config(type, prefix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T factory(TypeReference<? extends T> type, String prefix) {

		Type argType = type.getType();
		TypeRef<?> bqRef = new TypeRef<T>() {
			{
				this.type = argType;
			}
		};
		return (T) configFactory.config(bqRef, prefix);
	}
}
