package io.bootique.jackson;

import org.junit.Test;

import java.io.IOException;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OffsetTimeDeserializerIT extends DeserializerIT {

    @Test
    public void testDeserialization01() throws Exception {
        OffsetTime offsetTime = OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(1));

        OffsetTime value = this.mapper.readValue("\"" + offsetTime + "\"", OffsetTime.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", offsetTime, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        OffsetTime offsetTime = OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(1));

        OffsetTime value = this.mapper.readValue("\"10:15:30+01:00\"", OffsetTime.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", offsetTime, value);
    }

    @Test
    public void testDeserialization03() throws IOException {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  offsetTime: 10:15:30+01:00");
        assertEquals(OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(1)), bean1.c.offsetTime);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        OffsetTime offsetTime = OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(1));

        this.mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = this.mapper.readValue("[\"" + OffsetTime.class.getName() + "\",\"10:15:30+01:00\"]", TemporalAccessor.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a OffsetTime.", value instanceof OffsetTime);
        assertEquals("The value is not correct.", offsetTime, value);
    }

}
