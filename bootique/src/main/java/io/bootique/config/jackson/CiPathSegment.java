package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * A path segment for case-insensitive path.
 */
public class CiPathSegment extends PathSegment {

	protected CiPathSegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath) {
		super(node, parent, incomingPath, remainingPath);
	}

	public CiPathSegment(JsonNode node, String remainingPath) {
		super(node, remainingPath);
	}

	@Override
	protected PathSegment createChild(String incomingPath, String remainingPath) {

		String key = getNode() != null ? getChildCiKey(getNode(), incomingPath) : incomingPath;

		JsonNode child = getNode() != null ? getNode().get(key) : null;
		return new CiPathSegment(child, this, key, remainingPath);
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
