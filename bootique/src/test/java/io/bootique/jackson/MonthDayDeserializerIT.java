package io.bootique.jackson;


import org.junit.Test;

import java.io.IOException;
import java.time.Month;
import java.time.MonthDay;
import java.time.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MonthDayDeserializerIT extends DeserializerIT {

    @Test
    public void testDeserialization01() throws Exception {
        MonthDay monthDay = MonthDay.of(Month.JANUARY, 17);

        MonthDay value = this.mapper.readValue("\"--01-17\"", MonthDay.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", monthDay, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        MonthDay monthDay = MonthDay.of(Month.AUGUST, 21);

        MonthDay value = this.mapper.readValue("\"--08-21\"", MonthDay.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", monthDay, value);
    }

    @Test
    public void testDeserialization03() throws IOException {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  monthDay: --08-21");
        assertEquals(MonthDay.of(Month.AUGUST, 21), bean1.c.monthDay);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        MonthDay monthDay = MonthDay.of(Month.NOVEMBER, 5);

        this.mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = this.mapper.readValue("[\"" + MonthDay.class.getName() + "\",\"--11-05\"]", TemporalAccessor.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a MonthDay.", value instanceof MonthDay);
        assertEquals("The value is not correct.", monthDay, value);
    }

}
