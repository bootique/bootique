package io.bootique.jackson;


import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LocalDateTimeDeserializerIT extends DeserializerIT {

    @Test
    public void testDeserializationAsTimestamp04Milliseconds01() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: [2005,11,5,22,31,5,829837]");
        assertEquals(LocalDateTime.of(2005, Month.NOVEMBER, 5, 22, 31, 5, 829837), bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsString01() throws Exception {
        LocalDateTime time = LocalDateTime.of(1986, Month.JANUARY, 17, 15, 43);
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: " + time.toString());
        assertEquals(time, bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsString02() throws Exception {
        LocalDateTime time = LocalDateTime.of(2013, Month.AUGUST, 21, 9, 22, 57);
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: " + time.toString());
        assertEquals(time, bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsString03() throws Exception {
        LocalDateTime time = LocalDateTime.of(2005, Month.NOVEMBER, 5, 22, 31, 5, 829837);
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: " + time.toString());
        assertEquals(time, bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsString04() throws Exception {
        Instant instant = Instant.now();
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  instant: " + instant.toString());
        assertEquals(instant, bean1.c.instant);
    }

    @Test
    public void testDeserializationAsTimestamp01() throws IOException {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: [2017,8,25,15,43]");
        assertTrue(bean1 instanceof Bean1);
        assertEquals("x", bean1.a);
        assertEquals(LocalDateTime.of(2017, Month.AUGUST, 25, 15, 43), bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsTimestamp02() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: [2017,8,25,15,43,57]");
        assertEquals(LocalDateTime.of(2017, Month.AUGUST, 25, 15, 43, 57), bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsTimestamp03Nanoseconds() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: [2017,8,25,15,43,57,11]");
        assertEquals(LocalDateTime.of(2017, Month.AUGUST, 25, 15, 43, 57, 11), bean1.c.localDateTime);
    }

    @Test
    public void testDeserializationAsTimestamp04Nanoseconds() throws Exception {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDateTime: [2005,11,5,22,31,5,829837]");
        assertEquals(LocalDateTime.of(2005, Month.NOVEMBER, 5, 22, 31, 5, 829837), bean1.c.localDateTime);
    }

    @Test
    public void testDeserializeToDate() throws Exception {
        Date date = mapper.readValue("\"1999-10-12T13:45:05\"", Date.class);
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(date.getTime());
        assertEquals(1999, cal.get(Calendar.YEAR));
        assertEquals(12, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(5, cal.get(Calendar.SECOND));
    }

}
