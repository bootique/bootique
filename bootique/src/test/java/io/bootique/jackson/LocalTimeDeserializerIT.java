package io.bootique.jackson;

import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalTimeDeserializerIT extends DeserializerIT {

    @Test
    public void testDeserialization_Quoted() throws Exception {
        LocalTime time = LocalTime.of(9, 22, 0, 57);
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localTime: \"" + time + "\"");
        assertEquals(time, bean1.c.localTime);
    }

    @Ignore("SnakeYaml 1.18 thinks that XX:XX:XX is a float, not a String and chooses the wrong parser")
    @Test
    public void testDeserialization_Unquoted() throws Exception {
        LocalTime time = LocalTime.of(22, 31, 5, 829837);
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localTime: " + time.toString());
        assertEquals(time, bean1.c.localTime);
    }

    @Test
    public void testDeserializationAsTimestamp03Nanoseconds() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localTime: [9,22,0,57]");
        assertEquals(LocalTime.of(9, 22, 0, 57), bean1.c.localTime);
    }

    @Test
    public void testDeserializationAsTimestamp04Nanoseconds() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localTime: [22,31,5,829837]");
        assertEquals(LocalTime.of(22, 31, 5, 829837), bean1.c.localTime);
    }

    @Test
    public void testDeserializationAsTimestamp01() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localTime: [15,43]");
        assertTrue(bean1 instanceof Bean1);
        assertEquals("x", bean1.a);
        assertEquals(LocalTime.of(15, 43), bean1.c.localTime);
    }

    @Test
    public void testDeserializationAsTimestamp02() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localTime: [9,22,57]");
        assertEquals(LocalTime.of(9, 22, 57), bean1.c.localTime);
    }

}
