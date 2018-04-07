package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * A helper class to navigate {@link JsonNode} objects.
 */
abstract class PathSegment implements Iterable<PathSegment> {

    static final char DOT = '.';
    static final char ARRAY_INDEX_START = '[';
    static final char ARRAY_INDEX_END = ']';

    protected String remainingPath;
    protected JsonNode node;
    protected String incomingPath;
    protected PathSegment parent;

    protected PathSegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath) {
        this.node = node;
        this.parent = parent;
        this.incomingPath = incomingPath;
        this.remainingPath = remainingPath;
    }

    static PathSegment create(JsonNode node, String path) {

        if (path.length() == 0) {
            return new ValueSegment(node, null, null);
        }

        if (path.charAt(0) == ARRAY_INDEX_START) {
            return new IndexSegment(node, null, null, path);
        }

        return new PropertySegment(node, null, null, path);
    }

    public Optional<PathSegment> lastPathComponent() {
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

    protected PathSegment parseNext() {
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

    protected PathSegment createValueChild(String childName) {
        return new ValueSegment(readChild(childName), this, childName);
    }

    protected PathSegment createPropertyChild(String childName, String remainingPath) {
        return new PropertySegment(readChild(childName), this, childName, remainingPath);
    }

    protected PathSegment createIndexedChild(String childName, String remainingPath) {
        return new IndexSegment(readChild(childName), this, childName, remainingPath);
    }

    void fillMissingParents() {
        parent.fillMissingNodes(incomingPath, node, new JsonNodeFactory(true));
    }

    protected abstract void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory);

    @Override
    public Iterator<PathSegment> iterator() {
        return new Iterator<PathSegment>() {

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