package io.bootique.jackson;


import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class LocalDateDeserializerIT extends DeserializerTestBase {
    @Test
    public void testDeserialization() throws Throwable {
        Bean o = deserialize(Bean.class, "localDate: \"1986-01-17\"");
        assertEquals(LocalDate.of(1986, Month.JANUARY, 17), o.localDate);
    }

    static class Bean {

        protected LocalDate localDate;

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }
    }
}
