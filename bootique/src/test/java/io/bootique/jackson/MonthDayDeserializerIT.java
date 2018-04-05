package io.bootique.jackson;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.time.Month;
import java.time.MonthDay;
import java.time.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MonthDayDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization01() throws Exception {
        MonthDay monthDay = MonthDay.of(Month.JANUARY, 17);

        MonthDay value = deserialize(MonthDay.class, "\"--01-17\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", monthDay, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        MonthDay monthDay = MonthDay.of(Month.AUGUST, 21);

        MonthDay value = deserialize(MonthDay.class, "\"--08-21\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", monthDay, value);
    }

    @Test
    public void testDeserialization03() throws IOException {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  monthDay: --08-21");
        assertEquals(MonthDay.of(Month.AUGUST, 21), bean1.c.monthDay);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        MonthDay monthDay = MonthDay.of(Month.NOVEMBER, 5);

        ObjectMapper mapper = createMapper();
        mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = mapper.readValue("[\"" + MonthDay.class.getName() + "\",\"--11-05\"]", TemporalAccessor.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a MonthDay.", value instanceof MonthDay);
        assertEquals("The value is not correct.", monthDay, value);
    }

}
