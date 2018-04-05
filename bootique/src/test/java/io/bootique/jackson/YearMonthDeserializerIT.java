package io.bootique.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;

import java.time.Month;
import java.time.YearMonth;

import static org.junit.Assert.assertEquals;

public class YearMonthDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization_Value1() throws Exception {
        YearMonth ym = deserialize(YearMonth.class, "\"1986-01\"");
        assertEquals(YearMonth.of(1986, Month.JANUARY), ym);
    }

    @Test
    public void testDeserialization_Value2() throws Exception {
        YearMonth ym = deserialize(YearMonth.class, "\"2013-08\"");
        assertEquals(YearMonth.of(2013, Month.AUGUST), ym);
    }

    @Test
    public void testDeserialization_Pattern() throws Exception {
        YM_Pattern ym = deserialize(YM_Pattern.class, "yearMonth: \"1308\"");
        assertEquals(YearMonth.of(2013, Month.AUGUST), ym.yearMonth);
    }

    private static class YM_Pattern {
        @JsonProperty("yearMonth")
        @JsonFormat(pattern = "yyMM")
        final YearMonth yearMonth;

        @JsonCreator
        YM_Pattern(@JsonProperty("yearMonth") YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }
    }
}
