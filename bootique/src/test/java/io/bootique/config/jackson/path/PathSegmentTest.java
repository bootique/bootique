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
import io.bootique.config.jackson.YamlReader;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PathSegmentTest {

    @Test
    public void testLastPathComponent_Value() {
        JsonNode node = YamlReader.read("a");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("a", last.get().getNode().asText());
    }

    @Test
    public void testLastPathComponent_Value_PastEnd() {
        JsonNode node = YamlReader.read("a");
        assertThrows(IllegalArgumentException.class, () -> PathSegment.create(node, "x").lastPathComponent());
    }

    @Test
    public void testLastPathComponent_Root() {
        JsonNode node = YamlReader.read("a: b\nc: d");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("b", last.get().getNode().get("a").asText());
        assertEquals("d", last.get().getNode().get("c").asText());
    }

    @Test
    public void testLastPathComponent_Object_NullValue() {
        JsonNode node = YamlReader.read("a: null");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertTrue(last.get().getNode().get("a").isNull());
    }

    @Test
    public void testLastPathComponent_Object() {
        JsonNode node = YamlReader.read("a: b\nc: d");
        Optional<PathSegment<?>> last = PathSegment.create(node, "a").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("b", last.get().getNode().asText());
    }

    @Test
    public void testLastPathComponent_ObjectNested() {
        JsonNode node = YamlReader.read("a: b\nc:\n  d: e");
        Optional<PathSegment<?>> last = PathSegment.create(node, "c.d").lastPathComponent();

        assertNotNull(last);
        assertEquals("e", last.get().getNode().asText());
    }

    @Test
    public void testLastPathComponent_ArrayOutOfBounds() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> PathSegment.create(node, "a[-1]").lastPathComponent());
    }

    @Test
    public void testLastPathComponent_Array_NonNumericIndex() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalArgumentException.class, () -> PathSegment.create(node, "a[a]").lastPathComponent());
    }

    @Test
    public void testLastPathComponent_Array_MissingClosingParen1() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalStateException.class, () -> PathSegment.create(node, "a[1.").lastPathComponent());
    }

    @Test
    public void testLastPathComponent_Array_MissingClosingParen2() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalStateException.class, () -> PathSegment.create(node, "a[12").lastPathComponent());
    }

    @Test
    public void testLastPathComponent_Array_Nested_PropertyMissingDot() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalStateException.class, () -> PathSegment.create(node, "a[1]b").lastPathComponent());
    }

    @Test
    public void testLastPathComponent_ArrayRootValue() {
        JsonNode node = YamlReader.read("- 1\n- 2");

        this.assertCanCreateValidPathSegment(node, "[0]", 1);

        this.assertCanCreateValidPathSegment(node, "[1]", 2);
    }

    @Test
    public void testLastPathComponent_ArrayValue() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        this.assertCanCreateValidPathSegment(node, "a[0]", 1);

        this.assertCanCreateValidPathSegment(node, "a[1]", 2);
    }

    @Test
    public void testLastPathComponent_Array_PastEnd() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last = PathSegment.create(node, "a[2]").lastPathComponent();
        assertTrue(last.isPresent(), "Couldn't resolve 'a[2]' path");
        assertNull(last.get().getNode(), "Index past array end must resolve to an null element");
    }

    @Test
    public void testLastPathComponent_Array_PastEnd_Symbolic() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last = PathSegment.create(node, "a[.length]").lastPathComponent();
        assertTrue(last.isPresent(), "Couldn't resolve 'a[.length]' path");
        assertNull(last.get().getNode(), "Index past array end must resolve to an null element");
    }

    @Test
    public void testLastPathComponent_ArrayObject() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");

        this.assertCanCreateValidPathSegment(node, "a[1].b", 2);
    }

    public void assertCanCreateValidPathSegment(JsonNode t, String path, int expectedValue) {
        Optional<PathSegment<?>> last = PathSegment.create(t, path).lastPathComponent();
        assertTrue(last.isPresent(), "Couldn't resolve '" + path + "' path");
        assertNotNull(last.get().getNode(), "Couldn't resolve '" + path + "' path");
        assertEquals(expectedValue, last.get().getNode().asInt());
    }
}
