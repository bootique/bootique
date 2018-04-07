package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

class LastPathSegment extends PathSegment<JsonNode> {

    LastPathSegment(JsonNode node, PathSegment parent, String incomingPath) {
        super(node, parent, incomingPath, null);
    }

    @Override
    protected PathSegment parseNextNotEmpty(String path) {
        throw new UnsupportedOperationException("No more children");
    }

    @Override
    protected JsonNode createMissingNode() {
        throw new UnsupportedOperationException("This node does not support filling missing elements");
    }

    @Override
    JsonNode readChild(String childName) {
        throw new UnsupportedOperationException("This node is not supposed to have children");
    }

    @Override
    void writeChildValue(String childName, String value) {
        throw new UnsupportedOperationException("This node is not supposed to have children");
    }

    @Override
    void writeChild(String childName, JsonNode childNode) {
        throw new UnsupportedOperationException("This node is not supposed to have children");
    }
}
