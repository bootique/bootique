package com.nhl.bootique.config.jackson;

import java.io.IOException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.type.TypeRef;

/**
 * {@link ConfigurationFactory} based on Jackson {@link JsonNode} data
 * structure. The actual configuration can come from JSON, YAML, XML, etc.
 * 
 * @since 0.17
 */
public class JsonNodeConfigurationFactory implements ConfigurationFactory {

	private JsonNode rootNode;
	private ObjectMapper mapper;
	private TypeFactory typeFactory;

	public JsonNodeConfigurationFactory(JsonNode rootConfigNode, ObjectMapper objectMapper) {
		this.typeFactory = TypeFactory.defaultInstance();
		this.mapper = objectMapper;
		this.rootNode = rootConfigNode;
	}

	@Override
	public <T> T config(Class<T> type, String prefix) {

		JsonNode child = findChild(prefix);

		try {
			return mapper.readValue(new TreeTraversingParser(child), type);
		}
		// TODO: implement better exception handling. See ConfigurationFactory
		// in Dropwizard for inspiration
		catch (IOException e) {
			throw new RuntimeException("Error creating config", e);
		}
	}

	@Override
	public <T> T config(TypeRef<? extends T> type, String prefix) {

		JsonNode child = findChild(prefix);

		JavaType jacksonType = typeFactory.constructType(type.getType());

		try {
			return mapper.readValue(new TreeTraversingParser(child), jacksonType);
		}
		// TODO: implement better exception handling. See ConfigurationFactory
		// in Dropwizard for inspiration
		catch (IOException e) {
			throw new RuntimeException("Error creating config", e);
		}
	}

	protected JsonNode findChild(String path) {
		return new PathSegment(rootNode, path).lastPathComponent().map(t -> t.getNode()).orElse(new ObjectNode(null));
	}

}
