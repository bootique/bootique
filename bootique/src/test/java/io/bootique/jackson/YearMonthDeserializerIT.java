package io.bootique.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;

import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.Temporal;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class YearMonthDeserializerIT extends DeserializerIT {

    @Test
    public void testDeserializationAsTimestamp01() throws Exception {
        YearMonth yearMonth = YearMonth.of(1986, Month.JANUARY);
        YearMonth value = this.mapper.readValue("[1986,1]", YearMonth.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", yearMonth, value);
    }

    @Test
    public void testDeserializationAsTimestamp02() throws Exception {
        YearMonth yearMonth = YearMonth.of(2013, Month.AUGUST);
        YearMonth value = this.mapper.readValue("[2013,8]", YearMonth.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", yearMonth, value);
    }

    @Test
    public void testDeserializationAsString01() throws Exception {
        YearMonth yearMonth = YearMonth.of(1986, Month.JANUARY);
        YearMonth value = this.mapper.readValue('"' + yearMonth.toString() + '"', YearMonth.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", yearMonth, value);
    }

    @Test
    public void testDeserializationAsString02() throws Exception {
        YearMonth yearMonth = YearMonth.of(2013, Month.AUGUST);
        YearMonth value = this.mapper.readValue('"' + yearMonth.toString() + '"', YearMonth.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", yearMonth, value);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        YearMonth yearMonth = YearMonth.of(2005, Month.NOVEMBER);

        this.mapper.addMixIn(Temporal.class, MockObjectConfiguration.class);
        Temporal value = this.mapper.readValue("[\"" + YearMonth.class.getName() + "\",\"" + yearMonth.toString() + "\"]", Temporal.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a YearMonth.", value instanceof YearMonth);
        assertEquals("The value is not correct.", yearMonth, value);
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

    @Test
    public void testDeserializationWithPattern01() throws Exception {
        YearMonth yearMonth = YearMonth.of(2013, Month.AUGUST);
        SimpleAggregate simpleAggregate = new SimpleAggregate(yearMonth);

        SimpleAggregate value = this.mapper.readValue("{\"yearMonth\":\"1308\"}", SimpleAggregate.class);

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", simpleAggregate.yearMonth, value.yearMonth);
    }
}
