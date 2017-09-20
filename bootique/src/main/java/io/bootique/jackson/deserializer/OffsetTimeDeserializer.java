package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Deserializer for Java 8 temporal {@link OffsetTime}s.
 */
class OffsetTimeDeserializer extends JSR310DateTimeDeserializerBase<OffsetTime> {
    private static final long serialVersionUID = 1L;

    public static final OffsetTimeDeserializer INSTANCE = new OffsetTimeDeserializer();

    private OffsetTimeDeserializer() {
        this(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    protected OffsetTimeDeserializer(DateTimeFormatter dtf) {
        super(OffsetTime.class, dtf);
    }

    @Override
    protected JsonDeserializer<OffsetTime> withDateFormat(DateTimeFormatter dtf) {
        return new OffsetTimeDeserializer(dtf);
    }

    @Override
    public OffsetTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String string = parser.getText().trim();
            if (string.length() == 0) {
                return null;
            }
            return OffsetTime.parse(string, _formatter);
        }
        if (!parser.isExpectedStartArrayToken()) {
            throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
        }
        int hour = parser.nextIntValue(-1);
        if (hour == -1) {
            JsonToken t = parser.getCurrentToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            if (t != JsonToken.VALUE_NUMBER_INT) {
                _reportWrongToken(parser, context, JsonToken.VALUE_NUMBER_INT, "hours");
            }
            hour = parser.getIntValue();
        }
        int minute = parser.nextIntValue(-1);
        if (minute == -1) {
            JsonToken t = parser.getCurrentToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            if (t != JsonToken.VALUE_NUMBER_INT) {
                _reportWrongToken(parser, context, JsonToken.VALUE_NUMBER_INT, "minutes");
            }
            minute = parser.getIntValue();
        }
        int partialSecond = 0;
        int second = 0;
        if (parser.nextToken() == JsonToken.VALUE_NUMBER_INT) {
            second = parser.getIntValue();
            if (parser.nextToken() == JsonToken.VALUE_NUMBER_INT) {
                partialSecond = parser.getIntValue();

                parser.nextToken();
            }
        }
        if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
            OffsetTime t = OffsetTime.of(hour, minute, second, partialSecond, ZoneOffset.of(parser.getText()));
            if (parser.nextToken() != JsonToken.END_ARRAY) {
                _reportWrongToken(parser, context, JsonToken.END_ARRAY, "timezone");
            }
            return t;
        }
        throw context.wrongTokenException(parser, JsonToken.VALUE_STRING, "Expected string for TimeZone after numeric values");
    }
}
