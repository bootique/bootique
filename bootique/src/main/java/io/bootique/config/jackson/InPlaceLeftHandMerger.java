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
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.bootique.log.BootLogger;

import java.util.Iterator;
import java.util.function.BinaryOperator;

/**
 * A configuration merger that merges right-hand config argument into left-hand
 * argument. It will try to avoid copying objects as much as possible, so chunks
 * of left and right documents may end up merged in the resulting object. This
 * assumes an app that cares about the merge result and throws away both source
 * and target after the merge.
 * 
 * @since 0.17
 */
public class InPlaceLeftHandMerger implements BinaryOperator<JsonNode> {

	private BootLogger bootLogger;

	public InPlaceLeftHandMerger(BootLogger bootLogger) {
		this.bootLogger = bootLogger;
	}

	@Override
	public JsonNode apply(JsonNode target, JsonNode source) {

		if (target == null) {
			return source;
		}

		if(source == null) {
			return target;
		}

		if (source.getNodeType() != target.getNodeType()) {
			throw new RuntimeException(
					"Can't merge incompatible node types: " + target.getNodeType() + " vs. " + source.getNodeType());
		}

		switch (source.getNodeType()) {
		case ARRAY:
			return mergeArrays(target, source);
		case OBJECT:
			return mergeObjects(target, source);
		case BINARY:
		case BOOLEAN:
		case NULL:
		case NUMBER:
		case STRING:
			return mergeScalars(target, source);
		default:
			bootLogger.stderr("Skipping merging of unsupported JSON node: " + source.getNodeType());
		}

		return target;
	}

	protected JsonNode mergeArrays(JsonNode target, JsonNode source) {

		// TODO: here we implemented "replace" strategy... perhaps we need
		// alternative "append" strategy?

		// side effect - source becomes mutable..
		return source;
	}

	protected JsonNode mergeObjects(JsonNode target, JsonNode source) {

		ObjectNode targetObject = (ObjectNode) target;
		ObjectNode srcObject = (ObjectNode) source;

		Iterator<String> fieldNames = srcObject.fieldNames();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			JsonNode srcChild = srcObject.get(fieldName);
			JsonNode targetChild = targetObject.get(fieldName);

			targetObject.replace(fieldName, apply(targetChild, srcChild));
		}

		return target;
	}

	protected JsonNode mergeScalars(JsonNode target, JsonNode source) {
		// side effect - source becomes mutable
		return source;
	}
}
