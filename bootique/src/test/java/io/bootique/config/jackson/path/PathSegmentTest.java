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
    public void lastPathComponent_Value() {
        JsonNode node = YamlReader.read("a");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("a", last.get().getNode().asText());
    }

    @Test
    public void lastPathComponent_Value_PastEnd() {
        JsonNode node = YamlReader.read("a");
        assertThrows(IllegalArgumentException.class, () -> PathSegment.create(node, "x").lastPathComponent());
    }

    @Test
    public void lastPathComponent_Root() {
        JsonNode node = YamlReader.read("a: b\nc: d");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("b", last.get().getNode().get("a").asText());
        assertEquals("d", last.get().getNode().get("c").asText());
    }

    @Test
    public void lastPathComponent_Object_NullValue() {
        JsonNode node = YamlReader.read("a: null");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertTrue(last.get().getNode().get("a").isNull());
    }

    @Test
    public void lastPathComponent_Object() {
        JsonNode node = YamlReader.read("a: b\nc: d");
        Optional<PathSegment<?>> last = PathSegment.create(node, "a").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("b", last.get().getNode().asText());
    }

    @Test
    public void lastPathComponent_ObjectNested() {
        JsonNode node = YamlReader.read("a: b\nc:\n  d: e");
        Optional<PathSegment<?>> last = PathSegment.create(node, "c.d").lastPathComponent();

        assertNotNull(last);
        assertEquals("e", last.get().getNode().asText());
    }

    @Test
    public void lastPathComponent_DotsInPath() {
        JsonNode node = YamlReader.read("a:\n  b.c: 6");

        Optional<PathSegment<?>> last0 = PathSegment.create(node, "a.b\\.c").lastPathComponent();
        assertTrue(last0.isPresent(), "Couldn't resolve 'a.b\\.c\"' path");
        assertNotNull(last0.get().getNode(), "Couldn't resolve 'a.b\\.c\"' path");
        assertEquals(6, last0.get().getNode().asInt());
    }

    @Test
    public void lastPathComponent_ArrayOutOfBounds() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> PathSegment.create(node, "a[-1]").lastPathComponent());
    }

    @Test
    public void lastPathComponent_Array_NonNumericIndex() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalArgumentException.class, () -> PathSegment.create(node, "a[a]").lastPathComponent());
    }

    @Test
    public void lastPathComponent_Array_MissingClosingParen1() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalStateException.class, () -> PathSegment.create(node, "a[1.").lastPathComponent());
    }

    @Test
    public void lastPathComponent_Array_MissingClosingParen2() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalStateException.class, () -> PathSegment.create(node, "a[12").lastPathComponent());
    }

    @Test
    public void lastPathComponent_Array_Nested_PropertyMissingDot() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        assertThrows(IllegalStateException.class, () -> PathSegment.create(node, "a[1]b").lastPathComponent());
    }

    @Test
    public void lastPathComponent_ArrayRootValue() {
        JsonNode node = YamlReader.read("- 1\n- 2");

        Optional<PathSegment<?>> last0 = PathSegment.create(node, "[0]").lastPathComponent();
        assertTrue(last0.isPresent(), "Couldn't resolve '[0]' path");
        assertNotNull(last0.get().getNode(), "Couldn't resolve '[0]' path");
        assertEquals(1, last0.get().getNode().asInt());

        Optional<PathSegment<?>> last1 = PathSegment.create(node, "[1]").lastPathComponent();
        assertTrue(last1.isPresent(), "Couldn't resolve '[1]' path");
        assertNotNull(last1.get().getNode(), "Couldn't resolve '[1]' path");
        assertEquals(2, last1.get().getNode().asInt());
    }

    @Test
    public void lastPathComponent_ArrayValue() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last0 = PathSegment.create(node, "a[0]").lastPathComponent();
        assertTrue(last0.isPresent(), "Couldn't resolve 'a[0]' path");
        assertNotNull(last0.get().getNode(), "Couldn't resolve 'a[0]' path");
        assertEquals(1, last0.get().getNode().asInt());

        Optional<PathSegment<?>> last1 = PathSegment.create(node, "a[1]").lastPathComponent();
        assertTrue(last1.isPresent(), "Couldn't resolve 'a[1]' path");
        assertNotNull(last1.get().getNode(), "Couldn't resolve 'a[1]' path");
        assertEquals(2, last1.get().getNode().asInt());
    }

    @Test
    public void lastPathComponent_Array_PastEnd() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last = PathSegment.create(node, "a[2]").lastPathComponent();
        assertTrue(last.isPresent(), "Couldn't resolve 'a[2]' path");
        assertNull(last.get().getNode(), "Index past array end must resolve to an null element");
    }

    @Test
    public void lastPathComponent_Array_PastEnd_Symbolic() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last = PathSegment.create(node, "a[.length]").lastPathComponent();
        assertTrue(last.isPresent(), "Couldn't resolve 'a[.length]' path");
        assertNull(last.get().getNode(), "Index past array end must resolve to an null element");
    }

    @Test
    public void lastPathComponent_ArrayObject() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        Optional<PathSegment<?>> last = PathSegment.create(node, "a[1].b").lastPathComponent();

        assertTrue(last.isPresent(), "Couldn't resolve 'a[1].b' path");
        assertNotNull(last.get().getNode(), "Couldn't resolve 'a[1].b' path");
        assertEquals(2, last.get().getNode().asInt());
    }
}
