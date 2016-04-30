package com.nhl.bootique.config.jackson;

import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhl.bootique.config.jackson.JsonNodeUtils.PathTuple;

/**
 * 
 * An in-place overrider of JsonNode values from a map of properties.
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

			PathTuple target = JsonNodeUtils.lastPathComponent(t, e.getKey()).get();
			target.fillMissingParents();

			if (!(target.parent.node instanceof ObjectNode)) {
				throw new IllegalArgumentException("Invalid property '" + e.getKey() + "'");
			}

			ObjectNode parentObjectNode = (ObjectNode) target.parent.node;
			parentObjectNode.put(target.incomingPath, e.getValue());
		});

		return t;
	}
}
