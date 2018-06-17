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
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Deserializer for Java 8 temporal {@link Instant}s, {@link OffsetDateTime}, and {@link ZonedDateTime}s.
 */
class InstantDeserializer<T extends Temporal>
        extends JSR310DateTimeDeserializerBase<T> {
    private static final long serialVersionUID = 1L;

    public static final InstantDeserializer<Instant> INSTANT = new InstantDeserializer<>(
            Instant.class, DateTimeFormatter.ISO_INSTANT,
            Instant::from,
            a -> Instant.ofEpochMilli(a.value),
            a -> Instant.ofEpochSecond(a.integer, a.fraction),
            null
    );

    public static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new InstantDeserializer<>(
            OffsetDateTime.class, DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            OffsetDateTime::from,
            a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
            a -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
            (d, z) -> d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime()))
    );

    public static final InstantDeserializer<ZonedDateTime> ZONED_DATE_TIME = new InstantDeserializer<>(
            ZonedDateTime.class, DateTimeFormatter.ISO_ZONED_DATE_TIME,
            ZonedDateTime::from,
            a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
            a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
            ZonedDateTime::withZoneSameInstant
    );

    protected final Function<FromIntegerArguments, T> fromMilliseconds;

    protected final Function<FromDecimalArguments, T> fromNanoseconds;

    protected final Function<TemporalAccessor, T> parsedToValue;

    protected final BiFunction<T, ZoneId, T> adjust;

    protected InstantDeserializer(Class<T> supportedType,
                                  DateTimeFormatter parser,
                                  Function<TemporalAccessor, T> parsedToValue,
                                  Function<FromIntegerArguments, T> fromMilliseconds,
                                  Function<FromDecimalArguments, T> fromNanoseconds,
                                  BiFunction<T, ZoneId, T> adjust) {
        super(supportedType, parser);
        this.parsedToValue = parsedToValue;
        this.fromMilliseconds = fromMilliseconds;
        this.fromNanoseconds = fromNanoseconds;
        this.adjust = adjust == null ? ((d, z) -> d) : adjust;
    }

    @SuppressWarnings("unchecked")
    protected InstantDeserializer(InstantDeserializer<T> base, DateTimeFormatter f) {
        super((Class<T>) base.handledType(), f);
        parsedToValue = base.parsedToValue;
        fromMilliseconds = base.fromMilliseconds;
        fromNanoseconds = base.fromNanoseconds;
        adjust = base.adjust;
    }

    @Override
    protected JsonDeserializer<T> withDateFormat(DateTimeFormatter dtf) {
        if (dtf == _formatter) {
            return this;
        }
        return new InstantDeserializer<T>(this, dtf);
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        //NOTE: Timestamps contain no timezone info, and are always in configured TZ. Only
        //string values have to be adjusted to the configured TZ.
        switch (parser.getCurrentTokenId()) {
            case JsonTokenId.ID_NUMBER_FLOAT: {
                BigDecimal value = parser.getDecimalValue();
                long seconds = value.longValue();
                int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
                return fromNanoseconds.apply(new FromDecimalArguments(
                        seconds, nanoseconds, getZone(context)));
            }

            case JsonTokenId.ID_NUMBER_INT: {
                long timestamp = parser.getLongValue();
                return this.fromNanoseconds.apply(new FromDecimalArguments(
                        timestamp, 0, this.getZone(context)
                ));

            }

            case JsonTokenId.ID_STRING: {
                String string = parser.getText().trim();
                if (string.length() == 0) {
                    return null;
                }
                T value;
                try {
                    TemporalAccessor acc = _formatter.parse(string);
                    value = parsedToValue.apply(acc);
                } catch (DateTimeException e) {
                    throw _peelDTE(e);
                }
                return value;
            }
        }
        throw context.mappingException("Expected type float, integer, or string.");
    }

    private ZoneId getZone(DeserializationContext context) {
        // Instants are always in UTC, so don't waste compute cycles
        return (_valueClass == Instant.class) ? null : context.getTimeZone().toZoneId();
    }

    private static class FromIntegerArguments {
        public final long value;
        public final ZoneId zoneId;

        private FromIntegerArguments(long value, ZoneId zoneId) {
            this.value = value;
            this.zoneId = zoneId;
        }
    }

    private static class FromDecimalArguments {
        public final long integer;
        public final int fraction;
        public final ZoneId zoneId;

        private FromDecimalArguments(long integer, int fraction, ZoneId zoneId) {
            this.integer = integer;
            this.fraction = fraction;
            this.zoneId = zoneId;
        }
    }
}
