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

package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * A path segment with remaining path being an array index.
 */
class IndexPathSegment extends PathSegment<ArrayNode> {

    // a symbolic index that allows to append values to array without knowing the length
    private static final String PAST_END_INDEX = ".length";

    protected IndexPathSegment(ArrayNode node, PathSegment parent, String incomingPath, String remainingPath) {
        super(node, parent, incomingPath, remainingPath);

        if (remainingPath != null) {

            if (remainingPath.length() < 3) {
                throw new IllegalArgumentException("The path must start with array index [NNN]. Instead got: " + remainingPath);
            }

            if (remainingPath.charAt(0) != ARRAY_INDEX_START) {
                throw new IllegalArgumentException("The path must start with array index [NNN]. Instead got: " + remainingPath);
            }
        }
    }

    @Override
    protected PathSegment parseNextNotEmpty(String path) {
        int len = path.length();

        // looking for ']' or '].'
        // start at index 1.. The first char is known to be '['
        for (int i = 1; i < len; i++) {
            char c = path.charAt(i);

            if (c == IndexPathSegment.ARRAY_INDEX_END) {

                // 1. [NNN]
                if (i == len - 1) {
                    return createValueChild(path.substring(0, i + 1));
                }
                // 2. [NNN].aaaa (i.e. in the second case the dot must follow closing paren)
                else if (path.charAt(i + 1) == PathSegment.DOT) {
                    return createPropertyChild(path.substring(0, i + 1), path.substring(i + 2));
                }
                // 3. [NNN][MMM] TODO => createIndexedChild
                // 4. Invalid path
                else {
                    throw new IllegalStateException("Invalid path after array index: " + path);
                }
            }
        }

        throw new IllegalStateException("No closing array index parenthesis: " + path);
    }

    @Override
    protected ArrayNode createMissingNode() {
        return new ArrayNode(NODE_FACTORY);
    }

    @Override
    JsonNode readChild(String childName) {
        return node != null ? node.get(toIndex(childName)) : null;
    }

    @Override
    void writeChildValue(String childName, String value) {
        JsonNode childNode = value == null ? node.nullNode() : node.textNode(value);
        writeChild(childName, childNode);
    }

    @Override
    void writeChild(String childName, JsonNode childNode) {
        int index = toIndex(childName);

        // allow replacing elements at index
        if (index < node.size()) {
            node.set(index, childNode);
        }
        // allow appending elements to the end of the array...
        else if (index == node.size()) {
            node.add(childNode);
        } else {
            throw new ArrayIndexOutOfBoundsException("Array index out of bounds: " + index + ". Size: " + node.size());
        }
    }

    protected int toIndex(String indexWithParenthesis) {

        if (indexWithParenthesis.length() < 3) {
            throw new IllegalArgumentException("Invalid array index. Must be in format [NNN]. Instead got " + indexWithParenthesis);
        }

        String indexString = indexWithParenthesis.substring(1, indexWithParenthesis.length() - 1);

        // format: [.length] or [NNN]

        if (PAST_END_INDEX.equals(indexString)) {
            return node.size();
        }

        int index;
        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException nfex) {
            throw new IllegalArgumentException("Non-int array index. Must be in format [NNN]. Instead got " + indexWithParenthesis);
        }

        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Invalid negative array index: " + indexWithParenthesis);
        }

        return index;
    }
}
