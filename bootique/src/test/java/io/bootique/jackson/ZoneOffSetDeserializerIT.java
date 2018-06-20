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
import org.junit.Test;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZoneOffSetDeserializerIT extends DeserializerTestBase {
    @Test
    public void testDeserialization01() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(17, 10);

        ZoneOffset value = deserialize(ZoneOffset.class, "\"+17:10\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", zoneOffset, value);
    }

    @Test
    public void testDeserialization02() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.of("+17:10");

        ZoneOffset value = deserialize(ZoneOffset.class, "\"" + zoneOffset.toString() + "\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", zoneOffset, value);
    }

    @Test
    public void testDeserialization03() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(17, 10);

        ZoneOffset value = deserialize(ZoneOffset.class, "\"" + zoneOffset.toString() + "\"");

        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", zoneOffset, value);
    }

    @Test
    public void testDeserialization04() throws IOException {
        Bean1 bean1 = deserialize(Bean1.class, "a: \"x\"\n" +
                "c:\n" +
                "  zoneOffset: +17:10");
        assertEquals(ZoneOffset.ofHoursMinutes(17, 10), bean1.c.zoneOffset);
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(17, 10);

        ObjectMapper mapper = createMapper();
        mapper.addMixIn(TemporalAccessor.class, MockObjectConfiguration.class);
        TemporalAccessor value = mapper.readValue("[\"" + ZoneOffset.class.getName() + "\",\"+17:10\"]", TemporalAccessor.class);

        assertNotNull("The value should not be null.", value);
        assertTrue("The value should be a ZoneOffset.", value instanceof ZoneOffset);
        assertEquals("The value is not correct.", zoneOffset, value);
    }
}
