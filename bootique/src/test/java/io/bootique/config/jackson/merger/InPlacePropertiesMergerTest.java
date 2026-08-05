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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InPlacePropertiesMergerTest {

    @Test
    public void apply_InPlace() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a: 5");
        JsonNode overridden = overrider.apply(node);
        assertSame(node, overridden, "Overriding must happen in place");
    }

    @Test
    public void apply() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").asInt());
    }

    @Test
    public void apply_Nested() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a:\n  b: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void apply_MissingRecreated() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

        JsonNode node = YamlReader.read("a:");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void apply_ObjectArray() {

        Map<String, String> props = Collections.singletonMap("a[1]", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

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
    public void apply_ObjectArray_PastEnd() {

        Map<String, String> props = Collections.singletonMap("a[2]", "50");
        InPlacePropertiesMerger overrider = new InPlacePropertiesMerger(props);

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

    @Test
    public void apply_ObjectArray_ManyIndices() {

        // properties are applied in the map iteration order, that is generally unrelated to the array index order,
        // so make sure the merger sorts them. Using a reversed insertion order to model the worst case
        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 24; i >= 0; i--) {
            props.put("a[" + i + "]", String.valueOf(i * 10));
        }

        JsonNode node = YamlReader.read("a:");
        new InPlacePropertiesMerger(props).apply(node);

        ArrayNode array = (ArrayNode) node.get("a");
        assertEquals(25, array.size());
        for (int i = 0; i < 25; i++) {
            assertEquals(i * 10, array.get(i).asInt());
        }
    }

    @Test
    public void apply_NestedObjectArray_ManyIndices() {

        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 12; i >= 0; i--) {
            props.put("a[" + i + "].b", String.valueOf(i));
        }

        JsonNode node = YamlReader.read("a:");
        new InPlacePropertiesMerger(props).apply(node);

        ArrayNode array = (ArrayNode) node.get("a");
        assertEquals(13, array.size());
        for (int i = 0; i < 13; i++) {
            assertEquals(i, array.get(i).get("b").asInt());
        }
    }

    @Test
    public void comparePaths() {

        // the paths as a plain lexicographic sort would order them
        List<String> paths = new ArrayList<>(List.of(
                "a",
                "a.b",
                "a.b[.length]",
                "a.b[0]",
                "a.b[0].c",
                "a.b[100]",
                "a.b[10]",
                "a.b[10].c",
                "a.b[11]",
                "a.b[1]",
                "a.b[2]",
                "a.b[9]",
                "a.c",
                "b"));

        paths.sort(InPlacePropertiesMerger.PATH_ORDER);

        assertEquals(List.of(
                "a",
                "a.b",
                "a.b[0]",
                "a.b[0].c",
                "a.b[1]",
                "a.b[2]",
                "a.b[9]",
                "a.b[10]",
                "a.b[10].c",
                "a.b[11]",
                "a.b[100]",
                "a.b[.length]",
                "a.c",
                "b"), paths);
    }

    @Test
    public void apply_ObjectArray_PastEndAndIndices() {

        // "[.length]" appends to the end, so it must be applied after the explicit indices, regardless of the map order
        Map<String, String> props = new LinkedHashMap<>();
        props.put("a[.length]", "X");
        props.put("a[1]", "50");
        props.put("a[0]", "40");

        JsonNode node = YamlReader.read("a:");
        new InPlacePropertiesMerger(props).apply(node);

        ArrayNode array = (ArrayNode) node.get("a");
        assertEquals(3, array.size());
        assertEquals(40, array.get(0).asInt());
        assertEquals(50, array.get(1).asInt());
        assertEquals("X", array.get(2).asText());
    }

    @Test
    public void comparePaths_PastEndIndex() {

        assertTrue(InPlacePropertiesMerger.comparePaths("a[0]", "a[.length]") < 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a[.length]", "a[0]") > 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a[100]", "a[.length]") < 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a[.length].b", "a[3].b") > 0);

        // ... yet it doesn't reorder unrelated paths
        assertEquals(0, InPlacePropertiesMerger.comparePaths("a[.length]", "a[.length]"));
        assertTrue(InPlacePropertiesMerger.comparePaths("a[.length]", "b[0]") < 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a.length", "a[0]") < 0);
    }

    @Test
    public void comparePaths_DigitsOutsideIndex() {

        // digits in property names are compared as characters. Their order is irrelevant to the merger, so there's no
        // reason to treat them as numbers
        assertTrue(InPlacePropertiesMerger.comparePaths("a.b10", "a.b2") < 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a10.b", "a2.b") < 0);

        // ... while the indices within such properties are still numbers
        assertTrue(InPlacePropertiesMerger.comparePaths("a.b10[2]", "a.b10[10]") < 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a[2].b10", "a[10].b2") < 0);
    }

    @Test
    public void comparePaths_LeadingZeros() {

        assertTrue(InPlacePropertiesMerger.comparePaths("a[007]", "a[10]") < 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a[10]", "a[007]") > 0);
        assertTrue(InPlacePropertiesMerger.comparePaths("a[01]", "a[2]") < 0);

        // paths that differ only in leading zeros denote the same array index, so their relative order is arbitrary.
        // Still it must be stable and antisymmetric to keep the comparator valid
        assertEquals(0, InPlacePropertiesMerger.comparePaths("a[0]", "a[0]"));
        int cmp = InPlacePropertiesMerger.comparePaths("a[00]", "a[0]");
        assertTrue(cmp != 0);
        assertEquals(-Integer.signum(cmp), Integer.signum(InPlacePropertiesMerger.comparePaths("a[0]", "a[00]")));
    }
}
