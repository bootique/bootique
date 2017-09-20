package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Deserializer for Java 8 temporal {@link LocalTime}s.
 */
class LocalTimeDeserializer extends JSR310DateTimeDeserializerBase<LocalTime> {
    private static final long serialVersionUID = 1L;

    public static final LocalTimeDeserializer INSTANCE = new LocalTimeDeserializer();

    private LocalTimeDeserializer() {
        this(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public LocalTimeDeserializer(DateTimeFormatter formatter) {
        super(LocalTime.class, formatter);
    }

    @Override
    protected JsonDeserializer<LocalTime> withDateFormat(DateTimeFormatter formatter) {
        return new LocalTimeDeserializer(formatter);
    }

    @Override
    public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String string = parser.getText().trim();
            if (string.length() == 0) {
                return null;
            }
            return LocalTime.parse(string, _formatter);
        }
        if (parser.isExpectedStartArrayToken()) {
            if (parser.nextToken() == JsonToken.END_ARRAY) {
                return null;
            }
            int hour = parser.getIntValue();

            parser.nextToken();
            int minute = parser.getIntValue();

            if (parser.nextToken() != JsonToken.END_ARRAY) {
                int second = parser.getIntValue();

                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    int partialSecond = parser.getIntValue();

                    if (parser.nextToken() != JsonToken.END_ARRAY)
                        throw context.wrongTokenException(parser, JsonToken.END_ARRAY, "Expected array to end.");

                    return LocalTime.of(hour, minute, second, partialSecond);
                }

                return LocalTime.of(hour, minute, second);
            }
            return LocalTime.of(hour, minute);
        }
        throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
    }
}
