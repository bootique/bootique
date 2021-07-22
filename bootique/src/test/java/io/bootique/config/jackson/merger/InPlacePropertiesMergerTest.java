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

package io.bootique.config.jackson.merger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.bootique.config.jackson.YamlReader;
import io.bootique.config.jackson.merger.InPlacePropertiesMerger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class InPlacePropertiesMergerTest {

    @Test
    public void testApply_InPlace() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a: 5");
        JsonNode overridden = overrider.apply(node);
        assertSame(node, overridden, "Overriding must happen in place");
    }

    @Test
    public void testApply() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").asInt());
    }

    @Test
    public void testApply_Nested() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a:\n  b: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void testApply_MissingRecreated() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a:");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void testApply_ObjectArray() {
        this.testApply_ObjectArrayTemplate(new ArrayList<Integer>(Arrays.asList(1, 50, 10)), "a[1]", "50", "a:\n" + "  - 1\n" + "  - 5\n" + "  - 10", "a");
    }

    @Test
    public void testApply_ObjectArray_PastEnd() {
        this.testApply_ObjectArrayTemplate(new ArrayList<Integer>(Arrays.asList(1, 5, 50)), "a[2]", "50", "a:\n" + "  - 1\n" + "  - 5", "a");
    }

    public void testApply_ObjectArrayTemplate(ArrayList<Integer> expectedArray, String key, String value, String arrayContent, String arrayName) {
        Map<String, String> props = Collections.singletonMap(key, value);
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);
        JsonNode node = YamlReader.read(arrayContent);
        overrider.apply(node);
        ArrayNode array = (ArrayNode) node.get(arrayName);
        ArrayList<Integer> actualArray = new ArrayList<Integer>();
        array.elements().forEachRemaining(i -> actualArray.add(i.asInt()));
        assertEquals(expectedArray, actualArray);
    }
}
