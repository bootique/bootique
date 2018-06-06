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


import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class LocalDateDeserializerIT extends DeserializerTestBase {
    @Test
    public void testDeserialization() throws Throwable {
        Bean o = deserialize(Bean.class, "localDate: \"1986-01-17\"");
        assertEquals(LocalDate.of(1986, Month.JANUARY, 17), o.localDate);
    }

    static class Bean {

        protected LocalDate localDate;

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }
    }
}
