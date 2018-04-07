package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PathSegmentTest {

    @Test
    public void testLastPathComponent_Value() {
        JsonNode node = YamlReader.read("a");
        Optional<PathSegment<?>> last = PathSegment.create(node, "").lastPathComponent();

        assertNotNull(last.get());
        assertEquals("a", last.get().getNode().asText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastPathComponent_Value_PastEnd() {
        JsonNode node = YamlReader.read("a");
        PathSegment.create(node, "x").lastPathComponent();
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

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testLastPathComponent_ArrayOutOfBounds() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        PathSegment.create(node, "a[-1]").lastPathComponent();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastPathComponent_Array_NonNumericIndex() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        PathSegment.create(node, "a[a]").lastPathComponent();
    }

    @Test(expected = IllegalStateException.class)
    public void testLastPathComponent_Array_MissingClosingParen1() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        PathSegment.create(node, "a[1.").lastPathComponent();
    }

    @Test(expected = IllegalStateException.class)
    public void testLastPathComponent_Array_MissingClosingParen2() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        PathSegment.create(node, "a[12").lastPathComponent();
    }

    @Test(expected = IllegalStateException.class)
    public void testLastPathComponent_Array_Nested_PropertyMissingDot() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        PathSegment.create(node, "a[1]b").lastPathComponent();
    }

    @Test
    public void testLastPathComponent_ArrayRootValue() {
        JsonNode node = YamlReader.read("- 1\n- 2");

        Optional<PathSegment<?>> last0 = PathSegment.create(node, "[0]").lastPathComponent();
        assertTrue("Couldn't resolve '[0]' path", last0.isPresent());
        assertNotNull("Couldn't resolve '[0]' path", last0.get().getNode());
        assertEquals(1, last0.get().getNode().asInt());

        Optional<PathSegment<?>> last1 = PathSegment.create(node, "[1]").lastPathComponent();
        assertTrue("Couldn't resolve '[1]' path", last1.isPresent());
        assertNotNull("Couldn't resolve '[1]' path", last1.get().getNode());
        assertEquals(2, last1.get().getNode().asInt());
    }

    @Test
    public void testLastPathComponent_ArrayValue() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last0 = PathSegment.create(node, "a[0]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[0]' path", last0.isPresent());
        assertNotNull("Couldn't resolve 'a[0]' path", last0.get().getNode());
        assertEquals(1, last0.get().getNode().asInt());

        Optional<PathSegment<?>> last1 = PathSegment.create(node, "a[1]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[1]' path", last1.isPresent());
        assertNotNull("Couldn't resolve 'a[1]' path", last1.get().getNode());
        assertEquals(2, last1.get().getNode().asInt());
    }

    @Test
    public void testLastPathComponent_Array_PastEnd() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last = PathSegment.create(node, "a[2]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[2]' path", last.isPresent());
        assertNull("Index past array end must resolve to an null element", last.get().getNode());
    }

    @Test
    public void testLastPathComponent_Array_PastEnd_Symbolic() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment<?>> last = PathSegment.create(node, "a[.length]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[.length]' path", last.isPresent());
        assertNull("Index past array end must resolve to an null element", last.get().getNode());
    }

    @Test
    public void testLastPathComponent_ArrayObject() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        Optional<PathSegment<?>> last = PathSegment.create(node, "a[1].b").lastPathComponent();

        assertTrue("Couldn't resolve 'a[1].b' path", last.isPresent());
        assertNotNull("Couldn't resolve 'a[1].b' path", last.get().getNode());
        assertEquals(2, last.get().getNode().asInt());
    }
}
