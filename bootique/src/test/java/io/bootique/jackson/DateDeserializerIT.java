package io.bootique.jackson;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialize() throws Exception {
        Date date = deserialize(Date.class, "\"1999-10-12T13:45:05\"");
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
