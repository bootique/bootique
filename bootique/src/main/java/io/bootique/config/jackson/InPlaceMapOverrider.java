package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from a map of properties.
 * 
 * @since 0.17
 */
public class InPlaceMapOverrider implements Function<JsonNode, JsonNode> {

	private Map<String, String> properties;

	public InPlaceMapOverrider(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public JsonNode apply(JsonNode t) {
		properties.entrySet().forEach(e -> {

			PathSegment target = lastPathComponent(t, e.getKey());
			target.fillMissingParents();

			if (!(target.getParentNode() instanceof ObjectNode)) {
				throw new IllegalArgumentException("Invalid property '" + e.getKey() + "'");
			}

			ObjectNode parentObjectNode = (ObjectNode) target.getParentNode();
			parentObjectNode.put(target.getIncomingPath(), e.getValue());
		});

		return t;
	}

	protected PathSegment lastPathComponent(JsonNode t, String path) {
		return new PathSegment(t, path).lastPathComponent().get();
	}
}
