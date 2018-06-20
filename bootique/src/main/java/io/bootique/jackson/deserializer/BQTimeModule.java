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

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * An adaptation of {@code com.fasterxml.jackson.datatype.jsr310.JavaTimeModule} from Jackson. Registers capability of
 * serializing {@code java.time} objects with the Jackson core.
 * <p>
 * Most {@code java.time} types are serialized as numbers (integers or decimals as appropriate) if the
 * {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} feature is enabled, and otherwise are serialized in
 * standard <a href="http://en.wikipedia.org/wiki/ISO_8601" target="_blank">ISO-8601</a> string representation. ISO-8601 specifies formats
 * for representing offset dates and times, zoned dates and times, local dates and times, periods, durations, zones, and more. All
 * {@code java.time} types have built-in translation to and from ISO-8601 formats.
 * <p>
 * Granularity of timestamps is controlled through the companion features
 * {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS} and
 * {@link com.fasterxml.jackson.databind.DeserializationFeature#READ_DATE_TIMESTAMPS_AS_NANOSECONDS}. For serialization, timestamps are
 * written as fractional numbers (decimals), where the number is seconds and the decimal is fractional seconds, if
 * {@code WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS} is enabled (it is by default), with resolution as fine as nanoseconds depending on the
 * underlying JDK implementation. If {@code WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS} is disabled, timestamps are written as a whole number of
 * milliseconds. At deserialization time, decimal numbers are always read as fractional second timestamps with up-to-nanosecond resolution,
 * since the meaning of the decimal is unambiguous. The more ambiguous integer types are read as fractional seconds without a decimal point
 * if {@code READ_DATE_TIMESTAMPS_AS_NANOSECONDS} is enabled (it is by default), and otherwise they are read as milliseconds.
 * <p>
 * Some exceptions to this standard serialization/deserialization rule:
 * <ul>
 * <li>{@link Period}, which always results in an ISO-8601 format because Periods must be represented in years, months, and/or days.</li>
 * <li>{@link Year}, which only contains a year and cannot be represented with a timestamp.</li>
 * <li>{@link YearMonth}, which only contains a year and a month and cannot be represented with a timestamp.</li>
 * <li>{@link MonthDay}, which only contains a month and a day and cannot be represented with a timestamp.</li>
 * <li>{@link ZoneId} and {@link ZoneOffset}, which do not actually store dates and times but are supported with this module nonetheless.</li>
 * <li>{@link LocalDate}, {@link LocalTime}, {@link LocalDateTime}, and {@link OffsetTime}, which cannot portably be converted to timestamps
 * and are instead represented as arrays when WRITE_DATES_AS_TIMESTAMPS is enabled.</li>
 * </ul>
 */
@SuppressWarnings("javadoc")
public final class BQTimeModule extends SimpleModule {

    public BQTimeModule() {
        super(PackageVersion.VERSION);

        // First deserializers

        // // Instant variants:
        addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        addDeserializer(OffsetDateTime.class, InstantDeserializer.OFFSET_DATE_TIME);
        addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME);

        // // Other deserializers
        addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
        addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
        addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
        addDeserializer(LocalTime.class, LocalTimeDeserializer.INSTANCE);
        addDeserializer(MonthDay.class, JSR310StringParsableDeserializer.MONTH_DAY);
        addDeserializer(OffsetTime.class, OffsetTimeDeserializer.INSTANCE);
        addDeserializer(Period.class, JSR310StringParsableDeserializer.PERIOD);
        addDeserializer(Year.class, YearDeserializer.INSTANCE);
        addDeserializer(YearMonth.class, YearMonthDeserializer.INSTANCE);
        addDeserializer(ZoneId.class, JSR310StringParsableDeserializer.ZONE_ID);
        addDeserializer(ZoneOffset.class, JSR310StringParsableDeserializer.ZONE_OFFSET);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addValueInstantiators(new ValueInstantiators.Base() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config,
                                                           BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                JavaType type = beanDesc.getType();
                Class<?> raw = type.getRawClass();

                // 15-May-2015, tatu: In theory not safe, but in practice we do need to do "fuzzy" matching
                // because we will (for now) be getting a subtype, but in future may want to downgrade
                // to the common base type. Even more, serializer may purposefully force use of base type.
                // So... in practice it really should always work, in the end. :)
                if (ZoneId.class.isAssignableFrom(raw)) {
                    // let's assume we should be getting "empty" StdValueInstantiator here:
                    if (defaultInstantiator instanceof StdValueInstantiator) {
                        StdValueInstantiator inst = (StdValueInstantiator) defaultInstantiator;
                        // one further complication: we need ZoneId info, not sub-class
                        AnnotatedClass ac;
                        if (raw == ZoneId.class) {
                            ac = beanDesc.getClassInfo();
                        } else {
                            // we don't need Annotations, so constructing directly is fine here
                            // even if it's not generally recommended
                            ac = AnnotatedClassResolver.resolve(config,
                                    config.constructType(ZoneId.class), config);
                        }
                        if (!inst.canCreateFromString()) {
                            AnnotatedMethod factory = _findFactory(ac, "of", String.class);
                            if (factory != null) {
                                inst.configureFromStringCreator(factory);
                            }
                            // otherwise... should we indicate an error?
                        }
                        // return ZoneIdInstantiator.construct(config, beanDesc, defaultInstantiator);
                    }
                }
                return defaultInstantiator;
            }
        });
    }

    protected AnnotatedMethod _findFactory(AnnotatedClass cls, String name, Class<?>... argTypes) {
        final int argCount = argTypes.length;
        for (AnnotatedMethod method : cls.getFactoryMethods()) {
            if (!name.equals(method.getName())
                    || (method.getParameterCount() != argCount)) {
                continue;
            }
            for (int i = 0; i < argCount; ++i) {
                Class<?> argType = method.getParameter(i).getRawType();
                if (!argType.isAssignableFrom(argTypes[i])) {
                    continue;
                }
            }
            return method;
        }
        return null;
    }
}
