package io.bootique.config.jackson;

import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * Overrides JsonNode object values from a map of properties.
 * 
 * @since 0.17
 */
public class InPlaceMapOverrider implements Function<JsonNode, JsonNode> {

	private Map<String, String> properties;
	private boolean caseSensitive;
	private char pathSeparator;

	public InPlaceMapOverrider(Map<String, String> properties, boolean caseSensitive, char pathSeparator) {
		this.properties = properties;
		this.caseSensitive = caseSensitive;
		this.pathSeparator = pathSeparator;
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
		PathSegment root = caseSensitive ? new PathSegment(t, path, pathSeparator)
				: new CiPathSegment(t, path, pathSeparator);
		return root.lastPathComponent().get();
	}
}
