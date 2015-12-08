package com.nhl.launcher.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultJacksonService implements JacksonService {

	@Override
	public ObjectMapper newObjectMapper() {
		// TODO see Dropwizard Jackson class - there are lots of extensions...
		return new ObjectMapper();
	}

}
