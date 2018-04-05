package io.bootique.jackson;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.time.Month;
import java.time.MonthDay;
import java.time.temporal.TemporalAccessor;

import static org.junit.Assert.assertEquals;

public class MonthDayDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization_Value1() throws Exception {
        MonthDay md = deserialize(MonthDay.class, "\"--01-17\"");
        assertEquals(MonthDay.of(Month.JANUARY, 17), md);
    }

    @Test
    public void testDeserialization_Value2() throws Exception {
        MonthDay md = deserialize(MonthDay.class, "\"--08-21\"");
        assertEquals(MonthDay.of(Month.AUGUST, 21), md);
    }

    @Test
    public void testDeserialization_Object() throws IOException {
        Bean md = deserialize(Bean.class, "monthDay: --08-21");
        assertEquals(MonthDay.of(Month.AUGUST, 21), md.monthDay);
    }

    @Test
    public void testDeserializationWithTypeInfo() throws Exception {

        ObjectMapper mapper = createMapper();
        mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = mapper.readValue("[\"" + MonthDay.class.getName() + "\",\"--11-05\"]", TemporalAccessor.class);

        assertEquals(MonthDay.of(Month.NOVEMBER, 5), value);
    }

    static class Bean {

        protected MonthDay monthDay;

        public void setMonthDay(MonthDay monthDay) {
            this.monthDay = monthDay;
        }
    }

}
