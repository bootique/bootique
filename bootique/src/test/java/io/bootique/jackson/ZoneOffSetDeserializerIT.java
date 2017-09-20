package io.bootique.jackson;

import org.junit.Test;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZoneOffSetDeserializerIT extends DeserializerIT {
    @Test
    public void testDeserialization01() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(17, 10);

        ZoneOffset value = this.mapper.readValue("\"+17:10\"", ZoneOffset.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", zoneOffset, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.of("+17:10");

        ZoneOffset value = this.mapper.readValue("\"" + zoneOffset.toString() + "\"", ZoneOffset.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", zoneOffset, value);
    }

    @Test
    public void testDeserialization03() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(17, 10);

        ZoneOffset value = this.mapper.readValue("\"" + zoneOffset.toString() + "\"", ZoneOffset.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", zoneOffset, value);
    }

    @Test
    public void testDeserialization04() throws IOException {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  zoneOffset: +17:10");
        assertEquals(ZoneOffset.ofHoursMinutes(17, 10), bean1.c.zoneOffset);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(17, 10);

        this.mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = this.mapper.readValue("[\"" + ZoneOffset.class.getName() + "\",\"+17:10\"]", TemporalAccessor.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a ZoneOffset.", value instanceof ZoneOffset);
        assertEquals("The value is not correct.", zoneOffset, value);
    }
}
