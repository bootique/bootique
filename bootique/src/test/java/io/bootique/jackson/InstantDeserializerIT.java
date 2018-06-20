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

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class InstantDeserializerIT extends DeserializerTestBase {

    @Test
    public void testDeserialize() throws Exception {
        Bean o = deserialize(Bean.class, "instant: \"2018-04-05T12:34:42.212Z\"");
        assertEquals(Instant.ofEpochMilli(1522931682212L), o.instant);
    }

    static class Bean {

        protected Instant instant;

        public void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}
