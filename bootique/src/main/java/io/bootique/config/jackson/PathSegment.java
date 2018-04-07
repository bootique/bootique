package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * A helper class to navigate {@link JsonNode} objects.
 */
abstract class PathSegment<T extends JsonNode> implements Iterable<PathSegment<?>> {

    static final char DOT = '.';
    static final char ARRAY_INDEX_START = '[';
    static final char ARRAY_INDEX_END = ']';

    protected String remainingPath;
    protected T node;
    protected String incomingPath;
    protected PathSegment parent;

    protected PathSegment(T node, PathSegment parent, String incomingPath, String remainingPath) {
        this.node = node;
        this.parent = parent;
        this.incomingPath = incomingPath;
        this.remainingPath = remainingPath;
    }

    static PathSegment<?> create(JsonNode node, String path) {

        if (path.length() == 0) {
            return new LastPathSegment(node, null, null);
        }

        if (path.charAt(0) == ARRAY_INDEX_START) {
            return new IndexPathSegment(toArrayNode(node), null, null, path);
        }

        return new PropertyPathSegment(toObjectNode(node), null, null, path);
    }

    protected static ArrayNode toArrayNode(JsonNode node) {
        if (node != null) {

            if (node.isNull()) {
                return null;
            }

            if (!(node instanceof ArrayNode)) {
                throw new IllegalArgumentException(
                        "Expected ARRAY node. Instead got " + node.getNodeType());
            }
        }

        return (ArrayNode) node;
    }

    protected static ObjectNode toObjectNode(JsonNode node) {
        if (node != null) {

            if (node.isNull()) {
                return null;
            }

            if (!(node instanceof ObjectNode)) {

                throw new IllegalArgumentException(
                        "Expected OBJECT node. Instead got " + node.getNodeType());
            }
        }

        return (ObjectNode) node;
    }

    public Optional<PathSegment<?>> lastPathComponent() {
        return StreamSupport.stream(spliterator(), false).reduce((a, b) -> b);
    }

    public JsonNode getNode() {
        return node;
    }

    public PathSegment getParent() {
        return parent;
    }

    public String getIncomingPath() {
        return incomingPath;
    }

    protected PathSegment<?> parseNext() {
        if (remainingPath == null) {
            return null;
        }

        int len = remainingPath.length();
        if (len == 0) {
            return null;
        }

        return parseNextNotEmpty(remainingPath);
    }

    protected abstract PathSegment parseNextNotEmpty(String path);

    abstract JsonNode readChild(String childName);

    abstract void writeChild(String childName, String value);

    protected PathSegment<JsonNode> createValueChild(String childName) {
        JsonNode child = readChild(childName);
        return new LastPathSegment(child, this, childName);
    }

    protected PathSegment<ObjectNode> createPropertyChild(String childName, String remainingPath) {
        ObjectNode on = toObjectNode(readChild(childName));
        return new PropertyPathSegment(on, this, childName, remainingPath);
    }

    protected PathSegment<ArrayNode> createIndexedChild(String childName, String remainingPath) {
        ArrayNode an = toArrayNode(readChild(childName));
        return new IndexPathSegment(an, this, childName, remainingPath);
    }

    void fillMissingParents() {
        parent.fillMissingNodes(incomingPath, node, new JsonNodeFactory(true));
    }

    protected abstract void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory);

    @Override
    public Iterator<PathSegment<?>> iterator() {
        return new Iterator<PathSegment<?>>() {

            private PathSegment current = PathSegment.this;
            private PathSegment next = current.parseNext();

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public PathSegment next() {

                if (!hasNext()) {
                    throw new NoSuchElementException("Past iterator end");
                }

                PathSegment r = current;
                current = next;
                next = current != null ? current.parseNext() : null;
                return r;
            }
        };
    }
}