package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * A path segment for case-insensitive path.
 */
class CiPropertySegment extends PropertySegment {

    protected CiPropertySegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath) {
        super(node, parent, incomingPath, remainingPath);
    }

    CiPropertySegment(JsonNode node, String remainingPath) {
        super(node, null, null, remainingPath);
    }

    @Override
    protected JsonNode readChild(String childName) {
        String key = getNode() != null ? getChildCiKey(getNode(), childName) : childName;
        return getNode() != null ? getNode().get(key) : null;
    }

    @Override
    protected PathSegment createIndexedChild(String childName, String remainingPath) {
        throw new UnsupportedOperationException("Indexed CI children are unsupported");
    }

    @Override
    protected PathSegment createPropertyChild(String childName, String remainingPath) {
        return new CiPropertySegment(readChild(childName), this, childName, remainingPath);
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
