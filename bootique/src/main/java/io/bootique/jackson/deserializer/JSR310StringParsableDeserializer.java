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

package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;
import java.time.MonthDay;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.Function;

/**
 * Deserializer for all Java 8 temporal {@link java.time} types that cannot be represented with numbers and that have
 * parse functions that can take {@link String}s.
 */
class JSR310StringParsableDeserializer<T> extends JSR310DeserializerBase<T> {

    public static final JSR310StringParsableDeserializer<MonthDay> MONTH_DAY =
            new JSR310StringParsableDeserializer<>(MonthDay.class, MonthDay::parse);

    public static final JSR310StringParsableDeserializer<Period> PERIOD =
            new JSR310StringParsableDeserializer<>(Period.class, Period::parse);

    public static final JSR310StringParsableDeserializer<ZoneId> ZONE_ID =
            new JSR310StringParsableDeserializer<>(ZoneId.class, ZoneId::of);

    public static final JSR310StringParsableDeserializer<ZoneOffset> ZONE_OFFSET =
            new JSR310StringParsableDeserializer<>(ZoneOffset.class, ZoneOffset::of);

    private final Function<String, T> parse;

    private JSR310StringParsableDeserializer(Class<T> supportedType, Function<String, T> parse) {
        super(supportedType);
        this.parse = parse;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String string = parser.getValueAsString().trim();
        if (string.length() == 0) {
            return null;
        }
        return this.parse.apply(string);
    }

    @Override
    public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer deserializer)
            throws IOException {
        /**
         * This is a nasty kludge right here, working around issues like
         * [datatype-jsr310#24]. But should work better than not having the work-around.
         */
        if (parser.getCurrentToken().isScalarValue()) {
            return deserialize(parser, context);
        }
        return deserializer.deserializeTypedFromAny(parser, context);
    }
}
