/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

import static org.junit.jupiter.api.Assertions.*;

public class OffsetDateTimeDeserializerIT extends DeserializerTestBase {

    @Test
    public void deserialization01() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1));

        OffsetDateTime value = deserialize(OffsetDateTime.class, "\"" + offsetDateTime.toString() + "\"");

        assertNotNull(value, "The value should not be null.");
        assertEquals(offsetDateTime, value, "The value is not correct.");
    }

    @Test
    public void deserialization02() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1));

        OffsetDateTime value = deserialize(OffsetDateTime.class, "\"2017-09-02T10:15:30+01:00\"");

        assertNotNull(value, "The value should not be null.");
        assertEquals(offsetDateTime, value, "The value is not correct.");
    }

    @Test
    public void deserialization03() throws IOException {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  offsetDateTime: 2017-09-02T10:15:30+01:00");
        assertEquals(OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1)), bean1.c.offsetDateTime);
    }

    @Test
    public void deserializationWithTypeInfo01() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2017, 9, 2, 10, 15, 30, 0, ZoneOffset.ofHours(1));

        ObjectMapper mapper = createMapper();
        mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = mapper.readValue("[\"" + OffsetDateTime.class.getName() + "\",\"2017-09-02T10:15:30+01:00\"]", TemporalAccessor.class);

        assertNotNull(value, "The value should not be null.");
        assertTrue(value instanceof OffsetDateTime, "The value should be a OffsetDateTime.");
        assertEquals(offsetDateTime, value, "The value is not correct.");
    }

}
