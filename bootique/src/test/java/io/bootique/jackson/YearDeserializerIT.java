package io.bootique.jackson;


import org.junit.Test;

import java.time.Year;

import static org.junit.Assert.assertEquals;

public class YearDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization01() throws Exception {
        Year value = deserialize(Year.class, "1986");
        assertEquals("The value is not correct.", Year.of(1986), value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  year: 2017");
        assertEquals("The value is not correct.", Year.of(2017), bean1.c.year);
    }
    @Test
    public void testDeserialization03() throws Exception {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  year: " + Year.of(2017).toString());
        assertEquals("The value is not correct.", Year.of(2017), bean1.c.year);
    }
}
