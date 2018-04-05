package io.bootique.jackson;


import org.junit.Test;

import java.time.Year;

import static org.junit.Assert.assertEquals;

public class YearDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization_Value() throws Exception {
        Year value = deserialize(Year.class, "1986");
        assertEquals(Year.of(1986), value);
    }

    @Test
    public void testDeserialization_Object() throws Exception {
        Bean o = deserialize(Bean.class, "year: 2017");
        assertEquals(Year.of(2017), o.year);
    }

    static class Bean {

        protected Year year;

        public void setYear(Year year) {
            this.year = year;
        }
    }
}
