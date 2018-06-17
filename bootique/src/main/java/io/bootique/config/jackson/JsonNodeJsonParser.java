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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * @since 0.17
 */
public class JsonNodeJsonParser implements Function<InputStream, Optional<JsonNode>> {

	private ObjectMapper mapper;

	public JsonNodeJsonParser(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Optional<JsonNode> apply(InputStream t) {
		try {
			return Optional.ofNullable(mapper.readTree(t));
		} catch (IOException e) {
			throw new RuntimeException("Error reading config data", e);
		}
	}

}
