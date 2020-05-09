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
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class InPlaceMapOverriderTest {

    @Test
    public void testApply_InPlace() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props);

        JsonNode node = YamlReader.read("a: 5");
        JsonNode overridden = overrider.apply(node);
        assertSame(node, overridden, "Overriding must happen in place");
    }

    @Test
    public void testApply() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props);

        JsonNode node = YamlReader.read("a: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").asInt());
    }

    @Test
    public void testApply_Nested() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props);

        JsonNode node = YamlReader.read("a:\n  b: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void testApply_MissingRecreated() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props);

        JsonNode node = YamlReader.read("a:");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void testApply_ObjectArray() {

        Map<String, String> props = Collections.singletonMap("a[1]", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props);

        JsonNode node = YamlReader.read("a:\n" +
                "  - 1\n" +
                "  - 5\n" +
                "  - 10");
        overrider.apply(node);

        ArrayNode array = (ArrayNode) node.get("a");
        assertEquals(3, array.size());
        assertEquals(1, array.get(0).asInt());
        assertEquals(50, array.get(1).asInt());
        assertEquals(10, array.get(2).asInt());
    }

    @Test
    public void testApply_ObjectArray_PastEnd() {

        Map<String, String> props = Collections.singletonMap("a[2]", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props);

        JsonNode node = YamlReader.read("a:\n" +
                "  - 1\n" +
                "  - 5");
        overrider.apply(node);

        ArrayNode array = (ArrayNode) node.get("a");
        assertEquals(3, array.size());
        assertEquals(1, array.get(0).asInt());
        assertEquals(5, array.get(1).asInt());
        assertEquals(50, array.get(2).asInt());
    }
}
