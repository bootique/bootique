package io.bootique.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OffsetDateTimeDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization01() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1));

        OffsetDateTime value = deserialize(OffsetDateTime.class, "\"" + offsetDateTime.toString() + "\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", offsetDateTime, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1));

        OffsetDateTime value = deserialize(OffsetDateTime.class, "\"2017-09-02T10:15:30+01:00\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", offsetDateTime, value);
    }

    @Test
    public void testDeserialization03() throws IOException {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  offsetDateTime: 2017-09-02T10:15:30+01:00");
        assertEquals(OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1)), bean1.c.offsetDateTime);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1));

        ObjectMapper mapper = createMapper();
        mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = mapper.readValue("[\"" + OffsetDateTime.class.getName() + "\",\"2017-09-02T10:15:30+01:00\"]", TemporalAccessor.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a OffsetDateTime.", value instanceof OffsetDateTime);
        assertEquals("The value is not correct.", offsetDateTime, value);
    }

}
