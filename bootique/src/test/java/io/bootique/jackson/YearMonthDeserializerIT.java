package io.bootique.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;

import java.time.Month;
import java.time.YearMonth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class YearMonthDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserializationAsString01() throws Exception {
        YearMonth yearMonth = YearMonth.of(1986, Month.JANUARY);
        YearMonth value = deserialize(YearMonth.class, '"' + yearMonth.toString() + '"');

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", yearMonth, value);
    }

    @Test
    public void testDeserializationAsString02() throws Exception {
        YearMonth yearMonth = YearMonth.of(2013, Month.AUGUST);
        YearMonth value = deserialize(YearMonth.class, '"' + yearMonth.toString() + '"');

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", yearMonth, value);
    }

    @Test
    public void testDeserializationWithPattern01() throws Exception {
        YearMonth yearMonth = YearMonth.of(2013, Month.AUGUST);
        SimpleAggregate simpleAggregate = new SimpleAggregate(yearMonth);

        SimpleAggregate value = deserialize(SimpleAggregate.class, "{\"yearMonth\":\"1308\"}");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", simpleAggregate.yearMonth, value.yearMonth);
    }

    private static class SimpleAggregate {
        @JsonProperty("yearMonth")
        @JsonFormat(pattern = "yyMM")
        final YearMonth yearMonth;

        @JsonCreator
        SimpleAggregate(@JsonProperty("yearMonth") YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }
    }
}
