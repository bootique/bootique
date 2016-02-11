package com.nhl.bootique.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JacksonService {

	ObjectMapper newObjectMapper();
}
