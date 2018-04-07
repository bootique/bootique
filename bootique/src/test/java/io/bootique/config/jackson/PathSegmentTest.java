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
    public void testLastPathComponent_Root() {
        JsonNode node = YamlReader.read("a: b\nc: d");
        Optional<PathSegment> last = new PathSegment(node, "").lastPathComponent();

        assertNotNull(last);
        assertNotNull(last.get());
        assertEquals("b", last.get().getNode().get("a").asText());
        assertEquals("d", last.get().getNode().get("c").asText());
    }

    @Test
    public void testLastPathComponent() {
        JsonNode node = YamlReader.read("a: b\nc: d");
        Optional<PathSegment> last = new PathSegment(node, "a").lastPathComponent();

        assertNotNull(last);
        assertNotNull(last.get());
        assertEquals("b", last.get().getNode().asText());
    }

    @Test
    public void testLastPathComponent_Nested() {
        JsonNode node = YamlReader.read("a: b\nc:\n  d: e");
        Optional<PathSegment> last = new PathSegment(node, "c.d").lastPathComponent();

        assertNotNull(last);
        assertEquals("e", last.get().getNode().asText());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testLastPathComponent_ArrayOutOfBounds() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        new PathSegment(node, "a[-1]").lastPathComponent();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastPathComponent_NonNumericIndex() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        new PathSegment(node, "a[a]").lastPathComponent();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastPathComponent_MissingClosingParen1() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        new PathSegment(node, "a[1.").lastPathComponent();
    }

    @Test(expected = IllegalStateException.class)
    public void testLastPathComponent_MissingClosingParen2() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        new PathSegment(node, "a[12").lastPathComponent();
    }

    @Test(expected = IllegalStateException.class)
    public void testLastPathComponent_NestedPropertyMissingDot() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        new PathSegment(node, "a[1]b").lastPathComponent();
    }

    @Test
    public void testLastPathComponent_ArrayRootValue() {
        JsonNode node = YamlReader.read("- 1\n- 2");

        Optional<PathSegment> last0 = new PathSegment(node, "[0]").lastPathComponent();
        assertTrue("Couldn't resolve '[0]' path", last0.isPresent());
        assertNotNull("Couldn't resolve '[0]' path", last0.get().getNode());
        assertEquals(1, last0.get().getNode().asInt());

        Optional<PathSegment> last1 = new PathSegment(node, "[1]").lastPathComponent();
        assertTrue("Couldn't resolve '[1]' path", last1.isPresent());
        assertNotNull("Couldn't resolve '[1]' path", last1.get().getNode());
        assertEquals(2, last1.get().getNode().asInt());
    }

    @Test
    public void testLastPathComponent_ArrayValue() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment> last0 = new PathSegment(node, "a[0]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[0]' path", last0.isPresent());
        assertNotNull("Couldn't resolve 'a[0]' path", last0.get().getNode());
        assertEquals(1, last0.get().getNode().asInt());

        Optional<PathSegment> last1 = new PathSegment(node, "a[1]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[1]' path", last1.isPresent());
        assertNotNull("Couldn't resolve 'a[1]' path", last1.get().getNode());
        assertEquals(2, last1.get().getNode().asInt());
    }

    @Test
    public void testLastPathComponent_Array_PastEnd() {
        JsonNode node = YamlReader.read("a:\n  - 1\n  - 2");

        Optional<PathSegment> last2 = new PathSegment(node, "a[2]").lastPathComponent();
        assertTrue("Couldn't resolve 'a[2]' path", last2.isPresent());
        assertNull("Index past array end must resolve to an null element", last2.get().getNode());
    }

    @Test
    public void testLastPathComponent_ArrayObject() {
        JsonNode node = YamlReader.read("a:\n  - b: 1\n  - b: 2");
        Optional<PathSegment> last = new PathSegment(node, "a[1].b").lastPathComponent();

        assertTrue("Couldn't resolve 'a[1].b' path", last.isPresent());
        assertNotNull("Couldn't resolve 'a[1].b' path", last.get().getNode());
        assertEquals(2, last.get().getNode().asInt());
    }
}
