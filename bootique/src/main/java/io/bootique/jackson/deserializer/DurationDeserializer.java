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

package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * Deserializer for Java 8 temporal {@link Duration}s.
 */
class DurationDeserializer extends JSR310DeserializerBase<Duration> {

    public static final DurationDeserializer INSTANCE = new DurationDeserializer();

    private DurationDeserializer() {
        super(Duration.class);
    }

    @Override
    public Duration deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.getCurrentTokenId()) {
            case JsonTokenId.ID_NUMBER_FLOAT:
                BigDecimal value = parser.getDecimalValue();
                long seconds = value.longValue();
                int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
                return Duration.ofSeconds(seconds, nanoseconds);

            case JsonTokenId.ID_NUMBER_INT:
                return Duration.ofSeconds(parser.getLongValue());

            case JsonTokenId.ID_STRING:
                String string = parser.getText().trim();
                if (string.length() == 0) {
                    return null;
                }
                return Duration.parse(string);
        }
        throw JsonMappingException.from(parser, "Expected type float, integer, or string.");
    }
}
