package com.nhl.bootique.config.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 0.17
 */
public class JsonNodeJsonParser implements Function<InputStream, JsonNode> {

	private ObjectMapper mapper;

	public JsonNodeJsonParser(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public JsonNode apply(InputStream t) {
		try {
			return mapper.readTree(t);
		} catch (IOException e) {
			throw new RuntimeException("Error reading config data", e);
		}
	}

}
