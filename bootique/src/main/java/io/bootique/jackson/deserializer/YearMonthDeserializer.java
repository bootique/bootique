/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;


/**
 * Deserializer for Java 8 temporal {@link YearMonth}s.
 *
 * @author Nick Williams
 */
class YearMonthDeserializer extends JSR310DateTimeDeserializerBase<YearMonth> {
    private static final long serialVersionUID = 1L;

    public static final YearMonthDeserializer INSTANCE = new YearMonthDeserializer();

    private YearMonthDeserializer() {
        this(DateTimeFormatter.ofPattern("uuuu-MM"));
    }

    public YearMonthDeserializer(DateTimeFormatter formatter) {
        super(YearMonth.class, formatter);
    }

    @Override
    protected JsonDeserializer<YearMonth> withDateFormat(DateTimeFormatter dtf) {
        return new YearMonthDeserializer(dtf);
    }


    @Override
    public YearMonth deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.isExpectedStartArrayToken()) {
            int year = parser.nextIntValue(-1);
            if (year == -1) {
                if (parser.hasToken(JsonToken.END_ARRAY)) {
                    return null;
                }
                if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                    _reportWrongToken(parser, context, JsonToken.VALUE_NUMBER_INT, "years");
                }
                year = parser.getIntValue();
            }
            int month = parser.nextIntValue(-1);
            if (month == -1) {
                if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                    _reportWrongToken(parser, context, JsonToken.VALUE_NUMBER_INT, "months");
                }
                month = parser.getIntValue();
            }
            if (parser.nextToken() != JsonToken.END_ARRAY) {
                throw context.wrongTokenException(parser, JsonToken.END_ARRAY,
                        "Expected array to end.");
            }
            return YearMonth.of(year, month);
        }
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String string = parser.getText().trim();
            if (string.length() == 0) {
                return null;
            }
            return YearMonth.parse(string, _formatter);
        }
        throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
    }
}
