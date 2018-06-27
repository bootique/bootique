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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.log.DefaultBootLogger;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class InPlaceLeftHandMergerTest {

	private InPlaceLeftHandMerger merger = new InPlaceLeftHandMerger(new DefaultBootLogger(false));

	private JsonNode parse(String json) {
		try {
			JsonParser parser = new JsonFactory().createParser(json);
			return new ObjectMapper().readTree(parser);
		} catch (IOException e) {
			throw new RuntimeException("Parse error", e);
		}
	}

	@Test
	public void testApply_MergeMap() {

		JsonNode target = parse("{\"a\":1}");
		JsonNode source = parse("{\"b\":{\"c\":2}}");

		JsonNode merged = merger.apply(target, source);
		assertSame(target, merged);
		assertEquals("{\"a\":1,\"b\":{\"c\":2}}", merged.toString());
	}

	@Test
	public void testApply_MergeList_Replace() {

		JsonNode target = parse("[1,2,5]");
		JsonNode source = parse("[4,6]");

		JsonNode merged = merger.apply(target, source);
		assertEquals("[4,6]", merged.toString());
	}

	@Test
	public void testApply_OverrideValues() {

		JsonNode target = parse("{\"a\":true,\"b\":\"string\",\"c\":5,\"d\":\"unchanged\"}");
		JsonNode source = parse("{\"a\":false,\"b\":\"string1\",\"c\":6}");

		JsonNode merged = merger.apply(target, source);
		assertEquals("{\"a\":false,\"b\":\"string1\",\"c\":6,\"d\":\"unchanged\"}", merged.toString());
	}

	@Test
	public void testApply_OverrideValues_Null() {

		JsonNode target = parse("{\"a\":null,\"b\":\"not_null\",\"c\":null}");
		JsonNode source = parse("{\"a\":\"not_null\",\"b\":null,\"c\":null}");

		JsonNode merged = merger.apply(target, source);
		assertEquals("{\"a\":\"not_null\",\"b\":null,\"c\":null}", merged.toString());
	}

	@Test
	public void testApply_MergeSubMap() {

		JsonNode target = parse("{\"a\":{\"b\":2}}");
		JsonNode source = parse("{\"a\":{\"c\":3}}");

		JsonNode merged = merger.apply(target, source);
		assertEquals("{\"a\":{\"b\":2,\"c\":3}}", merged.toString());
	}

	@Test
	public void testApply_MergeSubList_Replace() {

		JsonNode target = parse("{\"a\":[1,3,5]}");
		JsonNode source = parse("{\"a\":[4,6]}");

		JsonNode merged = merger.apply(target, source);
		assertEquals("{\"a\":[4,6]}", merged.toString());
	}
	
	@Test
	public void testApply_MultiMerge() {

		JsonNode target = parse("{\"a\":{\"b\":2},\"e\":6}");
		JsonNode source1 = parse("{\"a\":{\"b\":3,\"c\":true,\"d\":5}}");
		JsonNode source2 = parse("{\"a\":{\"b\":4,\"c\":false}}");

		JsonNode merged = merger.apply(target, source1);
		merged = merger.apply(merged, source2);
		assertEquals("{\"a\":{\"b\":4,\"c\":false,\"d\":5},\"e\":6}", merged.toString());
	}

}
