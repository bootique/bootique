/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.config.jackson.path;

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
public abstract class PathSegment<T extends JsonNode> implements Iterable<PathSegment<?>> {

    protected static final JsonNodeFactory NODE_FACTORY = new JsonNodeFactory(true);

    protected T node;
    protected String incomingPath;
    protected String path;
    protected PathSegment<?> parent;

    protected static boolean isSegmentSeparator(String string, int i) {
        // do not treat escaped dots as path separators
        return string.charAt(i) == '.' && (i == 0 || string.charAt(i - 1) != '\\');
    }

    protected static boolean isArrayIndexStart(String string, int i) {
        return string.charAt(i) == '[';
    }

    protected static boolean isArrayIndexEnd(String string, int i) {
        return string.charAt(i) == ']';
    }

    protected static String unescapeSegmentName(String name) {
        return name.replace("\\.", ".");
    }

    protected PathSegment(T node, PathSegment<?> parent, String incomingPath, String path) {
        this.node = node;
        this.parent = parent;
        this.incomingPath = incomingPath;
        this.path = path;
    }

    public static PathSegment<?> create(JsonNode node, String path) {

        if (path.length() == 0) {
            return new LastPathSegment(node, null, null);
        }

        if (isArrayIndexStart(path, 0)) {
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

    public PathSegment<?> getParent() {
        return parent;
    }

    public String getIncomingPath() {
        return incomingPath;
    }

    protected PathSegment<?> parseNext() {
        if (path == null) {
            return null;
        }

        int len = path.length();
        if (len == 0) {
            return null;
        }

        return parseNextNotEmpty(path);
    }

    protected abstract PathSegment<?> parseNextNotEmpty(String path);

    protected abstract JsonNode readChild(String childName);

    protected abstract void writeChild(String childName, JsonNode childNode);

    public abstract void writeChildValue(String childName, String value);

    protected PathSegment<JsonNode> createValueChild(String childName) {
        String unescaped = unescapeSegmentName(childName);
        JsonNode child = readChild(unescaped);
        return new LastPathSegment(child, this, unescaped);
    }

    protected PathSegment<ObjectNode> createPropertyChild(String childName, String remainingPath) {
        String unescaped = unescapeSegmentName(childName);
        ObjectNode on = toObjectNode(readChild(unescaped));
        return new PropertyPathSegment(on, this, unescaped, remainingPath);
    }

    protected PathSegment<ArrayNode> createIndexedChild(String childName, String remainingPath) {
        String unescaped = unescapeSegmentName(childName);
        ArrayNode an = toArrayNode(readChild(unescaped));
        return new IndexPathSegment(an, this, unescaped, remainingPath);
    }

    public void fillMissingParents() {
        parent.fillMissingNodes(incomingPath, node);
    }

    protected abstract T createMissingNode();

    protected final void fillMissingNodes(String field, JsonNode child) {

        if (node == null || node.isNull()) {
            node = createMissingNode();
            parent.fillMissingNodes(incomingPath, node);
        }

        if (child != null) {
            writeChild(field, child);
        }
    }

    @Override
    public Iterator<PathSegment<?>> iterator() {
        return new Iterator<>() {

            private PathSegment<?> current = PathSegment.this;
            private PathSegment<?> next = current.parseNext();

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public PathSegment<?> next() {

                if (!hasNext()) {
                    throw new NoSuchElementException("Past iterator end");
                }

                PathSegment<?> r = current;
                current = next;
                next = current != null ? current.parseNext() : null;
                return r;
            }
        };
    }
}