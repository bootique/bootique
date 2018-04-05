package io.bootique.jackson;

import org.junit.Test;

import java.io.IOException;
import java.time.Period;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PeriodDeserializerIT extends DeserializerTestBase {
    @Test
    public void testDeserialization01() throws Exception {
        Period period = Period.of(5, 2, 3);

        Period value = deserialize(Period.class, "\"" + period.toString() + "\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", period, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        Period period = Period.of(5, 8, 3);

        Period value = deserialize(Period.class, "\"P5Y8M3D\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", period, value);
    }

    @Test
    public void testDeserialization03() throws IOException {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  period: P5Y8M3D");
        assertEquals(Period.of(5, 8, 3), bean1.c.period);
    }
}
