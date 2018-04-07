package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

class LastPathSegment extends PathSegment<JsonNode> {

    LastPathSegment(JsonNode node, PathSegment parent, String incomingPath) {
        super(node, parent, incomingPath, null);
    }

    @Override
    protected PathSegment parseNextNotEmpty(String path) {
        throw new UnsupportedOperationException("No more children");
    }

    @Override
    protected void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory) {
        throw new UnsupportedOperationException("This node is not supposed to have children");
    }

    @Override
    JsonNode readChild(String childName) {
        throw new UnsupportedOperationException("This node is not supposed to have children");
    }

    @Override
    void writeChild(String childName, String value) {
        throw new UnsupportedOperationException("This node is not supposed to have children");
    }
}
