/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
