/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
