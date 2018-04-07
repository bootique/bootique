package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class PropertyPathSegment extends PathSegment<ObjectNode> {

    PropertyPathSegment(ObjectNode node, PathSegment parent, String incomingPath, String remainingPath) {
        super(node, parent, incomingPath, remainingPath);
    }

    @Override
    JsonNode readChild(String childName) {
        return node != null ? node.get(childName) : null;
    }

    @Override
    void writeChild(String childName, String value) {
        JsonNode childNode = value == null ? node.nullNode() : node.textNode(value);
        node.set(childName, childNode);
    }

    void writeChild(String childName, JsonNode childNode) {
        node.set(childName, childNode);
    }

    @Override
    protected PathSegment parseNextNotEmpty(String path) {

        int len = path.length();

        // Start at index 1, assuming at least one leading char is the property name.
        // Look for either '.' or '['.
        for (int i = 1; i < len; i++) {
            char c = path.charAt(i);
            if (c == DOT) {
                // split ppp.ppp into "ppp" and "ppp"
                return createPropertyChild(path.substring(0, i), path.substring(i + 1));
            }

            if (c == ARRAY_INDEX_START) {
                // split ppp[nnn].ppp into "ppp" and "[nnn].ppp"
                return createIndexedChild(path.substring(0, i), path.substring(i));
            }
        }

        // no separators found ... the whole path is the property name
        return createValueChild(path);
    }

    @Override
    protected void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory) {

        if (node == null || node.isNull()) {
            node = new ObjectNode(nodeFactory);
            parent.fillMissingNodes(incomingPath, node, nodeFactory);
        }

        if (child != null) {
            writeChild(field, child);
        }
    }
}
