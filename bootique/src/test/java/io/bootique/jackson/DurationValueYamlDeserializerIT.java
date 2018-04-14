package io.bootique.jackson;

import io.bootique.value.Duration;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DurationValueYamlDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization_AsString() throws IOException {
        Duration d = deserialize(Duration.class, "5s");
        assertEquals(java.time.Duration.ofSeconds(5), d.getDuration());
    }

    @Test
    public void testDeserialization_AsString_Object() throws IOException {
        Bean b = deserialize(Bean.class, "duration: '5 min'");
        assertEquals(java.time.Duration.ofMinutes(5), b.duration.getDuration());
    }

    @Test
    public void testDeserialization_AsLongMs() throws IOException {
        Duration d = deserialize(Duration.class, "500");
        assertEquals(java.time.Duration.ofMillis(500), d.getDuration());
    }

    @Test
    public void testDeserialization_AsLong_Object() throws IOException {
        Bean b = deserialize(Bean.class, "duration: 500");
        assertEquals(java.time.Duration.ofMillis(500), b.duration.getDuration());
    }

    static class Bean {

        protected Duration duration;

        public void setDuration(Duration duration) {
            this.duration = duration;
        }
    }
}
