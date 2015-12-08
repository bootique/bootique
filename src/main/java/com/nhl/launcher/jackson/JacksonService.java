package com.nhl.launcher.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JacksonService {

	ObjectMapper newObjectMapper();
}
