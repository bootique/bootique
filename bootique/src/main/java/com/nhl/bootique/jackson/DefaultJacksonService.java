package com.nhl.bootique.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.google.inject.Inject;
import com.nhl.bootique.config.PolymorphicConfiguration;
import com.nhl.bootique.log.BootLogger;

public class DefaultJacksonService implements JacksonService {

	private BootLogger bootLogger;

	@Inject
	public DefaultJacksonService(BootLogger bootLogger) {
		this.bootLogger = bootLogger;
	}

	@Override
	public ObjectMapper newObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSubtypeResolver(createResolver());
		return mapper;
	}

	protected SubtypeResolver createResolver() {
		return new SubtypeResolverFactory(getClass().getClassLoader(), PolymorphicConfiguration.class, bootLogger)
				.createResolver();
	}

}
