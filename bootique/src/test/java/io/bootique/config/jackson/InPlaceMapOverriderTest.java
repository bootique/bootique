package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class InPlaceMapOverriderTest {

    @Test
    public void testApply_InPlace() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props, '.');

        JsonNode node = YamlReader.read("a: 5");
        JsonNode overridden = overrider.apply(node);
        assertSame("Overriding must happen in place", node, overridden);
    }

    @Test
    public void testApply() {

        Map<String, String> props = Collections.singletonMap("a", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props, '.');

        JsonNode node = YamlReader.read("a: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").asInt());
    }

    @Test
    public void testApply_Nested() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props, '.');

        JsonNode node = YamlReader.read("a:\n  b: 5");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }

    @Test
    public void testApply_MissingRecreated() {

        Map<String, String> props = Collections.singletonMap("a.b", "50");
        InPlaceMapOverrider overrider = new InPlaceMapOverrider(props, '.');

        JsonNode node = YamlReader.read("a:");
        overrider.apply(node);

        assertEquals(50, node.get("a").get("b").asInt());
    }
}
