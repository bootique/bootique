package io.bootique.jackson;


import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class LocalDateDeserializerIT extends DeserializerIT {
    @Test
    public void testDeserialization01() throws Throwable {

        LocalDate date = LocalDate.of(1986, Month.JANUARY, 17);
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDate: " + date.toString());

        assertEquals(date, bean1.c.localDate);
    }

    @Test
    public void testDeserialization02() throws Throwable {
        Bean1 bean1 = readValue(Bean1.class, mapper, "a: \"x\"\n" +
                "c:\n" +
                "  localDate: [2000,01,01]");
        assertEquals(LocalDate.of(2000, 1, 1), bean1.c.localDate);
    }

}
