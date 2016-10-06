package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * @since 0.17
 */
public class JsonNodeJsonParser implements Function<InputStream, Optional<JsonNode>> {

	private ObjectMapper mapper;

	public JsonNodeJsonParser(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Optional<JsonNode> apply(InputStream t) {
		try {
			return Optional.ofNullable(mapper.readTree(t));
		} catch (IOException e) {
			throw new RuntimeException("Error reading config data", e);
		}
	}

}
