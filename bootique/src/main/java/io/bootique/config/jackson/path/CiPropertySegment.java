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
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * A path segment for case-insensitive path.
 */
public class CiPropertySegment extends PropertyPathSegment {

    public static PathSegment<?> create(JsonNode node, String path) {

        if (path.length() == 0) {
            return new LastPathSegment(node, null, null);
        }

        if (path.charAt(0) == ARRAY_INDEX_START) {
            return new IndexPathSegment(toArrayNode(node), null, null, path);
        }

        return new CiPropertySegment(toObjectNode(node), null, null, path);
    }

    protected CiPropertySegment(ObjectNode node, PathSegment parent, String incomingPath, String remainingPath) {
        super(node, parent, incomingPath, remainingPath);
    }

    @Override
    protected JsonNode readChild(String childName) {
        String key = getNode() != null ? getChildCiKey(getNode(), childName) : childName;
        return getNode() != null ? getNode().get(key) : null;
    }

    @Override
    protected PathSegment<ArrayNode> createIndexedChild(String childName, String remainingPath) {
        throw new UnsupportedOperationException("Indexed CI children are unsupported");
    }

    @Override
    protected PathSegment<ObjectNode> createPropertyChild(String childName, String remainingPath) {
        ObjectNode on = toObjectNode(readChild(childName));
        return new CiPropertySegment(on, this, childName, remainingPath);
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
