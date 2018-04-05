package io.bootique.jackson;

import org.junit.Test;

import java.io.IOException;
import java.time.Period;

import static org.junit.Assert.assertEquals;

public class PeriodDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization_Value1() throws Exception {
        Period p = deserialize(Period.class, "\"P5Y2M3D\"");
        assertEquals(Period.of(5, 2, 3), p);
    }

    @Test
    public void testDeserialization_Value2() throws Exception {
        Period p = deserialize(Period.class, "\"P5Y8M3D\"");
        assertEquals(Period.of(5, 8, 3), p);
    }

    @Test
    public void testDeserialization_Object() throws IOException {
        Bean p = deserialize(Bean.class, "period: P5Y8M3D");
        assertEquals(Period.of(5, 8, 3), p.period);
    }

    static class Bean {

        protected Period period;

        public void setPeriod(Period period) {
            this.period = period;
        }
    }
}
