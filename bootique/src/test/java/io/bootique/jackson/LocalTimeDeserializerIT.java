package io.bootique.jackson;

import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class LocalTimeDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization() throws Exception {
        Bean o = deserialize(Bean.class, "localTime: \"09:22:00.000000057\"");
        assertEquals(LocalTime.of(9, 22, 0, 57), o.localTime);
    }

    static class Bean {

        protected LocalTime localTime;

        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }
    }
}
