package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * A helper class to navigate {@link JsonNode} objects.
 */
class PathSegment implements Iterable<PathSegment> {

    static final char DOT = '.';
    static final char ARRAY_INDEX_START = '[';
    static final char ARRAY_INDEX_END = ']';

    protected String remainingPath;
    private String incomingPath;
    protected JsonNode node;
    private PathSegment parent;

    PathSegment(JsonNode node, String remainingPath) {
        this(node, null, null, remainingPath);
    }

    protected PathSegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath) {
        this.node = node;
        this.parent = parent;
        this.incomingPath = incomingPath;
        this.remainingPath = normalizeRemainingPath(remainingPath);
    }

    protected String normalizeRemainingPath(String remainingPath) {
        // strip trailing dot... (why do we need this?)
        return remainingPath != null && remainingPath.length() > 0 && remainingPath.charAt(remainingPath.length() - 1) == DOT
                ? remainingPath.substring(0, remainingPath.length() - 1)
                : remainingPath;
    }

    public Optional<PathSegment> lastPathComponent() {
        return StreamSupport.stream(spliterator(), false).reduce((a, b) -> b);
    }

    public JsonNode getNode() {
        return node;
    }

    public JsonNode getParentNode() {
        return parent.getNode();
    }

    public String getIncomingPath() {
        return incomingPath;
    }

    protected PathSegment createNext() {
        if (remainingPath == null) {
            return null;
        }

        int len = remainingPath.length();
        if (len == 0) {
            return null;
        }

        // looking for either '.' or '['
        // start at index 1, assuming at least one char is the property name
        for (int i = 1; i < len; i++) {
            char c = remainingPath.charAt(i);
            if (c == DOT) {
                // split ppp.ppp into "ppp" and "ppp"
                return createChild(remainingPath.substring(0, i), remainingPath.substring(i + 1));
            }

            if (c == ARRAY_INDEX_START) {
                // split ppp[nnn].ppp into "ppp" and "[nnn].ppp"
                return createArrayChild(remainingPath.substring(0, i), remainingPath.substring(i));
            }
        }

        // no more separators...
        return createChild(remainingPath, "");
    }

    protected PathSegment createChild(String childName, String remainingPath) {
        JsonNode child = node != null ? node.get(childName) : null;
        return new PathSegment(child, this, childName, remainingPath);
    }

    protected PathSegment createArrayChild(String childName, String remainingPath) {
        JsonNode child = node != null ? node.get(childName) : null;
        return new ArrayIndexPathSegment(child, this, childName, remainingPath);
    }

    void fillMissingParents() {
        parent.fillMissingNodes(incomingPath, node, new JsonNodeFactory(true));
    }

    void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory) {

        if (node == null || node.isNull()) {
            node = new ObjectNode(nodeFactory);
            parent.fillMissingNodes(incomingPath, node, nodeFactory);
        }

        if (child != null) {
            if (node instanceof ObjectNode) {
                ((ObjectNode) node).set(field, child);
            } else {
                throw new IllegalArgumentException(
                        "Node '" + incomingPath + "' is unexpected in the middle of the path");
            }
        }
    }

    @Override
    public Iterator<PathSegment> iterator() {
        return new Iterator<PathSegment>() {

            private PathSegment current = PathSegment.this;
            private PathSegment next = current.createNext();

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
                next = current != null ? current.createNext() : null;
                return r;
            }
        };
    }
}