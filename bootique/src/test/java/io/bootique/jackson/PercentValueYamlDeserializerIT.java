package io.bootique.jackson;

import io.bootique.value.Percent;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PercentValueYamlDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization_AsPercent() throws Exception {
        Percent p = deserialize(Percent.class, "5%");
        assertEquals(5, p.getPercent(), 0.00001);
    }

    @Test
    public void testDeserialization_AsDouble() throws Exception {
        Percent p = deserialize(Percent.class, "5.0");
        assertEquals(500., p.getPercent(), 0.00001);
    }

    @Test
    public void testDeserialization_AsInt() throws Exception {
        Percent p = deserialize(Percent.class, "5");
        assertEquals(500., p.getPercent(), 0.00001);
    }

    @Test
    public void testDeserialization_Object() throws IOException {
        Bean p = deserialize(Bean.class, "percent: -5.%");
        assertEquals(-5., p.percent.getPercent(), 0.00001);
    }

    static class Bean {

        protected Percent percent;

        public void setPercent(Percent percent) {
            this.percent = percent;
        }
    }
}
