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

import java.io.IOException;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/**
 * Deserializer for Java 8 temporal {@link Year}s.
 */
class YearDeserializer extends JSR310DeserializerBase<Year> {
    private static final long serialVersionUID = 1L;
    private DateTimeFormatter formatter;
    public static final YearDeserializer INSTANCE = new YearDeserializer();

    private YearDeserializer() {
        super(Year.class);
    }

    public YearDeserializer(DateTimeFormatter formatter) {
        super(Year.class);
        this.formatter = formatter;
    }

    @Override
    public Year deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (formatter == null) {
            return Year.of(parser.getValueAsInt());
        }
        return Year.parse(parser.getValueAsString(), formatter);
    }
}
