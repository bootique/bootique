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

        throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
    }
}
