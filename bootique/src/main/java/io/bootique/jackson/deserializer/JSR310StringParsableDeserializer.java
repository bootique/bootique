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
