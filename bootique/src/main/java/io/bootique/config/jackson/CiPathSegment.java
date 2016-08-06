package io.bootique.config.jackson;

import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A path segment for case-insensitive path.
 */
public class CiPathSegment extends PathSegment {

	protected CiPathSegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath,
			char pathSeparator) {
		super(node, parent, incomingPath, remainingPath, pathSeparator);
	}

	public CiPathSegment(JsonNode node, String remainingPath, char pathSeparator) {
		super(node, remainingPath, pathSeparator);
	}

	@Override
	protected PathSegment createChild(String incomingPath, String remainingPath) {

		String key = getNode() != null ? getChildCiKey(getNode(), incomingPath) : incomingPath;

		JsonNode child = getNode() != null ? getNode().get(key) : null;
		return new CiPathSegment(child, this, key, remainingPath, pathSeparator);
	}

	private String getChildCiKey(JsonNode parent, String fieldName) {

		fieldName = fieldName.toUpperCase();

		Iterator<Entry<String, JsonNode>> fields = parent.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> f = fields.next();
			if (fieldName.equalsIgnoreCase(f.getKey())) {
				return f.getKey();
			}
		}

		return fieldName;
	}

}
