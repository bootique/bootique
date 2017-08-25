package io.bootique.jackson;


import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class DurationDeserializerIT extends DeserializerIT {
    @Test
    public void testDeserialization() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  duration: P2DT3H4M");
        assertEquals(Duration.parse("P2DT3H4M"), bean1.c.duration);
    }

    @Test
    public void testDeserializationAsFloat() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  duration: PT3H44M58.000008374S");
        assertEquals(Duration.ofSeconds(13498L, 8374), bean1.c.duration);
    }

}
