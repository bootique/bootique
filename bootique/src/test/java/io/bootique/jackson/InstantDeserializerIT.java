package io.bootique.jackson;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class InstantDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialize() throws Exception {
        Bean o = deserialize(Bean.class, "instant: \"2018-04-05T12:34:42.212Z\"");
        assertEquals(Instant.ofEpochMilli(1522931682212L), o.instant);
    }

    static class Bean {

        protected Instant instant;

        public void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}
