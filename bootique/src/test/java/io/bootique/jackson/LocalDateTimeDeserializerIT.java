package io.bootique.jackson;


import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class LocalDateTimeDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization11() throws Exception {
        Bean o = deserialize(Bean.class, "localDateTime: \"1986-01-17T15:43\"");
        assertEquals(LocalDateTime.of(1986, Month.JANUARY, 17, 15, 43), o.localDateTime);
    }

    @Test
    public void testDeserialization2() throws Exception {
        Bean o = deserialize(Bean.class, "localDateTime: \"2013-08-21T09:22:57\"");
        assertEquals(LocalDateTime.of(2013, Month.AUGUST, 21, 9, 22, 57), o.localDateTime);
    }

    @Test
    public void testDeserialization_Nanoseconds() throws Exception {
        Bean o = deserialize(Bean.class, "localDateTime: \"2005-11-05T22:31:05.000829837\"");
        assertEquals(LocalDateTime.of(2005, Month.NOVEMBER, 5, 22, 31, 5, 829837), o.localDateTime);
    }

    static class Bean {

        protected LocalDateTime localDateTime;

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }
    }
}
