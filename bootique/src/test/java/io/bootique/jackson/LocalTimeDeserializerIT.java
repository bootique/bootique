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

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalTimeDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialization() throws Exception {
        Bean o = deserialize(Bean.class, "localTime: \"09:22:00.000000057\"");
        assertEquals(LocalTime.of(9, 22, 0, 57), o.localTime);
    }

    static class Bean {

        protected LocalTime localTime;

        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }
    }
}
