package io.bootique.jackson;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class DurationDeserializerIT extends DeserializerTestBase {
    @Test
    public void testDeserialization() throws Exception {
        Bean o = deserialize(Bean.class, "duration: P2DT3H4M");
        assertEquals(Duration.parse("P2DT3H4M"), o.duration);
    }

    @Test
    public void testDeserializationAsFloat() throws Exception {
        Bean o = deserialize(Bean.class, "duration: PT3H44M58.000008374S");
        assertEquals(Duration.ofSeconds(13498L, 8374), o.duration);
    }

    static class Bean {

        protected Duration duration;

        public void setDuration(Duration duration) {
            this.duration = duration;
        }
    }
}
