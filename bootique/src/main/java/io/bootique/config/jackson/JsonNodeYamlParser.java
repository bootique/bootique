package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * @since 0.17
 */
public class JsonNodeYamlParser implements Function<InputStream, Optional<JsonNode>> {

	private YAMLFactory yamlFactory;
	private ObjectMapper mapper;

	public JsonNodeYamlParser(ObjectMapper mapper) {
		this.mapper = mapper;
		this.yamlFactory = new YAMLFactory();
	}

	@Override
	public Optional<JsonNode> apply(InputStream t) {
		try {
			YAMLParser parser = yamlFactory.createParser(t);
			return Optional.ofNullable(mapper.readTree(parser));
		} catch (IOException e) {
			throw new RuntimeException("Error reading config data", e);
		}
	}
}
